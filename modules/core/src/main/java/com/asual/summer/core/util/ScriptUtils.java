package com.asual.summer.core.util;

import javax.inject.Named;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.shell.Global;

@Named
public class ScriptUtils {

	public static final String SCOPE_ATTRIBUTE = ScriptUtils.class.getName() + ".SCOPE_ATTRIBUTE";
	private static Scriptable scope;
	
	static {
		Context cx = Context.enter();
		cx.setOptimizationLevel(9);
		Global global = new Global();
		global.init(cx);		  
		scope = cx.initStandardObjects(global);
		Context.exit();
	}

	private static Scriptable getScope() {
		if (RequestUtils.getRequest() != null) {
			if (RequestUtils.getAttribute(SCOPE_ATTRIBUTE) == null) {
				Context cx = Context.enter();
				Scriptable requestScope = cx.newObject(scope);
			    requestScope.setPrototype(scope);
			    requestScope.setParentScope(null);
			    RequestUtils.setAttribute(SCOPE_ATTRIBUTE, requestScope);
			    Context.exit();
			}
			return (Scriptable) RequestUtils.getAttribute(SCOPE_ATTRIBUTE);
		}
		return scope;
	}
	
	public static boolean isDefined(String fn) {
		return getScope().get(fn, getScope()) != null;
	}
	
	public static void define(String fn) {
	    Context ctx = Context.enter();
		ctx.evaluateString(getScope(), fn, fn, 1, null);
		Context.exit();
	}

	public static Object call(String fn, Object... args) {
		return Context.call(null, (Function) getScope().get(fn, getScope()), getScope(), getScope(), args);
	}
	
	public static Object eval(String src) {
		Context ctx = Context.enter();
		Object result = ctx.evaluateString(getScope(), src, src, 1, null);
		Context.exit();
		return result;
	}
	
}