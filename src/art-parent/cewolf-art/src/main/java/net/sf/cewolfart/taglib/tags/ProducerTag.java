/* ================================================================
 * Cewolf : Chart enabling Web Objects Framework
 * ================================================================
 *
 * Project Info:  http://cewolf.sourceforge.net
 * Project Lead:  Guido Laures (guido@laures.de);
 *
 * (C) Copyright 2002, by Guido Laures
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package net.sf.cewolfart.taglib.tags;

import javax.servlet.jsp.JspException;

import net.sf.cewolfart.DatasetProducer;
import net.sf.cewolfart.taglib.DataAware;

/** 
 * Tag &lt;producer&gt; which defines a DatasetProducer.
 * @see DataTag
 * @author  Guido Laures 
 */
public class ProducerTag extends AbstractParameterizedObjectTag {

	static final long serialVersionUID = 1193241004439087455L;

    private boolean useCache = true;

    public int doEndTag() throws JspException {
        DatasetProducer dataProducer = null;
        try {
            dataProducer = (DatasetProducer) getObject();
            if (dataProducer == null) {
                throw new JspException("Could not find datasetproducer under ID '" + getId() + "'");
            }
        } catch (ClassCastException cce) {
            throw new JspException(
                "Bean under ID '"
                    + getId()
                    + "' is of type '"
                    + getObject().getClass().getName()
                    + "'.\nType expected:"
                    + DatasetProducer.class.getName());
        }
        DataAware dw = (DataAware) findAncestorWithClass(this, DataAware.class);
        addParameter(DatasetProducer.PRODUCER_ATTRIBUTE_NAME, getId(), null);
        dw.setDataProductionConfig(dataProducer, getParameters(), useCache);
        return doAfterEndTag(EVAL_BODY_INCLUDE);
    }

    /**
     * Sets the useCache.
     * @param useCache The useCache to set
     */
    public void setUsecache(boolean useCache) {
        this.useCache = useCache;
    }

}
