package de.hotware.hibernate.search.extension.reference.bytecode;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import javassist.ByteArrayClassPath;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class ObjectLoaderTransformer implements ClassFileTransformer {

	private static final Logger LOGGER = Logger
			.getLogger(ObjectLoaderTransformer.class.getName());

	private Lock lock = new ReentrantLock();
	private ClassPool classPool;

	public ObjectLoaderTransformer() {

	}

	private void init() {
		this.lock.lock();
		try {
			if (this.classPool == null) {
				ClassPool classPool = new ClassPool();
				classPool.appendSystemPath();
				try {
					classPool.appendPathList(System
							.getProperty("java.class.path"));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				this.classPool = classPool;
			}
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public byte[] transform(ClassLoader loader, String fullyQualifiedClassName,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classBytes) throws IllegalClassFormatException {
		this.lock.lock();
		try {
			this.init();
			String className = fullyQualifiedClassName.replace("/", ".");
			this.classPool.insertClassPath(new ByteArrayClassPath(className,
					classBytes));
			try {
				if (className
						.equals("org.hibernate.search.query.hibernate.impl.ObjectLoaderHelper")) {
					try {
						LOGGER.info("==========================");
						LOGGER.info("=found ObjectLoaderHelper=");
						LOGGER.info("==========================");
						CtClass ctClass = this.classPool.get(className);
						LOGGER.info("ctClass: " + ctClass);
						LOGGER.info("==========================");
						if (ctClass.isFrozen()) {
							LOGGER.warning("ObjectLoaderHelper is frozen, cannot change it!");
							return null;
						}
	
						if (ctClass.isPrimitive() || ctClass.isArray()
								|| ctClass.isAnnotation() || ctClass.isEnum()
								|| ctClass.isInterface()) {
							throw new AssertionError(
									"ObjectLoaderHelper is no class?");
						}
	
						boolean isClassModified = false;
						int found = 0;
						for (CtMethod method : ctClass.getDeclaredMethods()) {
							// if method is annotated, add the code to measure the
							// time
							if (method.getName().equals("executeLoad")) {
								if (++found <= 1) {
									// TODO: maybe get more specific with more
									// parameters?
									if (method.getMethodInfo().getCodeAttribute() == null) {
										LOGGER.warning("executeLoad had codeAttribute == null!");
										return null;
									}
									LOGGER.info("Instrumenting method "
											+ method.getLongName());
									addCache(ctClass, method);
									isClassModified = true;
								} else {
									throw new AssertionError(
											"executeLoad exists more than once!");
								}
							}
						}
						if (!isClassModified) {
							return null;
						}
						return ctClass.toBytecode();
					} finally {
						// we can delete everything after this.
						// and we have to make sure we are fine for an reload
						this.classPool = null;
					}
				}
			} catch (RuntimeException | CannotCompileException | IOException
					| NotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			return null;
		} finally {
			this.lock.unlock();
		}
	}

	private static void addCache(CtClass clazz, CtMethod oldMethod)
			throws NotFoundException, CannotCompileException {
		String methodName = oldMethod.getName();

		// rename old method to synthetic name, then duplicate the
		// method with original name for use as interceptor
		String oldMethodRenamed = methodName + "$impl";
		LOGGER.info("renaming old method.");
		oldMethod.setName(oldMethodRenamed);
		LOGGER.info("creating new method.");
		CtMethod mnew = CtNewMethod.copy(oldMethod, methodName, clazz, null);

		StringBuilder builder = new StringBuilder();
		builder.append("{")
				.append("de.hotware.hibernate.search.extension.reference.ReferenceCache __cache =")
				.append("	de.hotware.hibernate.search.extension.reference.ReferenceCacheFactory.getReferenceCache($1.getClazz());\n")
				.append("if(__cache != null) {\n")
				.append("	Object __returnValue = __cache.find($1.getId());\n")
				.append("	if(__returnValue == null) {\n")
				.append("		__returnValue = ").append(oldMethodRenamed)
				.append("($$);\n")
				.append("		__cache.addToCache($1.getId(), __returnValue);\n")
				.append("	}\n").append("	return __returnValue;\n")
				.append("} else {\n").append("	return ")
				.append(oldMethodRenamed).append("($$);\n").append("}")
				.append("}");

		LOGGER.info("setting method body.");
		// replace the body of the interceptor method with generated
		// code block and add it to class
		mnew.setBody(builder.toString());
		LOGGER.info("adding new method.");
		clazz.addMethod(mnew);

		LOGGER.info("Interceptor method body:");
		LOGGER.info(builder.toString());
	}

}
