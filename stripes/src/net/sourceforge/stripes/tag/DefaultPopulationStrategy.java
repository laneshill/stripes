package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.util.OgnlUtil;
import net.sourceforge.stripes.util.HtmlUtil;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.exception.StripesJspException;

import ognl.OgnlException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Default implementation of the form input tag population strategy. First looks to see if there
 *  is a parameter with the same name as the tag submitted in the current request.  If there is,
 * it will be returned as a String[] in order to support multiple-value parameters.  Values that
 * are pulled from the request for re-population purposes will automatically be filtered of
 * special HTML characters, so that they re-display properly and do not allow the user to inject
 * HTML into the page.</p>
 *
 * <p>If there is no value in the request then an ActionBean bound to the current form will be
 * looked for.  If the ActionBean is found and the value is non-null it will be returned.
 * If no value can be found in either place, null will returned.
 *
 * @author Tim Fennell
 */
public class DefaultPopulationStrategy implements PopulationStrategy {
    /** Log used to log any errors that occur. */
    private static Log log = LogFactory.getLog(DefaultPopulationStrategy.class);

    /**
     * Implementation of the interface method that will follow the search described in the class
     * level JavaDoc and attempt to find a value for this tag.
     *
     * @param tag the form input tag whose value to populate
     * @return Object will be one of null, a single Object or an Array of Objects depending upon
     *         what was submitted in the prior request, and what is declard on the ActionBean
     */
    public Object getValue(InputTagSupport tag) throws StripesJspException {
        // Look first for something that the user submitted in the current request
        final String[] paramValues = tag.getPageContext().getRequest().getParameterValues(tag.getName());

        if (paramValues != null) {
            for (int i=0; i<paramValues.length; ++i) {
                paramValues[i] = HtmlUtil.encode(paramValues[i]);
            }

            return paramValues;
        }
        else {
            // If that's not there, let's look on the ActionBean
            ActionBean action = tag.getParentFormTag().getActionBean();
            Object beanValue = null;

            if (action != null) {
                try {
                    beanValue = OgnlUtil.getValue(tag.getName(), action);
                }
                catch (OgnlException oe) {
                    log.debug("Could not locate property of name [" + tag.getName() + "] on ActionBean.", oe);
                }
            }

            return beanValue;
        }
    }
}
