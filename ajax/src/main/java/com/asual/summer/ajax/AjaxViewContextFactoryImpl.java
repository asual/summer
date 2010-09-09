package com.asual.summer.ajax;

import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.context.PartialViewContextFactory;

public class AjaxViewContextFactoryImpl extends PartialViewContextFactory {

	@Override
    public PartialViewContext getPartialViewContext(FacesContext context) {
        return new AjaxPartialViewContextImpl(context);
    }

}
