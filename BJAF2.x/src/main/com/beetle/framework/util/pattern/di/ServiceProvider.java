package com.beetle.framework.util.pattern.di;

import com.google.inject.Provider;

/**
 * 服务提供者(可灵活运行时根据逻辑选定实现) <br>
 * 以下情况采取provider方式<br>
 * •When the default means for obtaining instances (an injectable or
 * parameterless constructor) is insufficient for a particular binding, the
 * module can specify a custom Provider instead, to control exactly how Guice
 * creates or obtains instances for the binding. <br>
 * •An implementation class may always choose to have a Provider<T> instance
 * injected, rather than having a T injected directly. This may give you access
 * to multiple instances, instances you wish to safely mutate and discard,
 * instances which are out of scope (e.g. using a @RequestScoped object from
 * within a @SessionScoped object), or instances that will be initialized
 * lazily. <br>
 * •A custom Scope is implemented as a decorator of Provider<T>, which decides
 * when to delegate to the backing provider and when to provide the instance
 * some other way. <br>
 * 
 * @author HenryYu
 * 
 * @param <T>
 */
public abstract class ServiceProvider<T> implements Provider<T> {
}
