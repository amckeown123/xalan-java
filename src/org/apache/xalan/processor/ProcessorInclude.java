/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.xalan.processor;

import org.apache.xalan.utils.TreeWalker;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import java.net.URL;
import java.io.IOException;
import org.xml.sax.helpers.XMLReaderFactory;
import trax.URIResolver;
import org.w3c.dom.Node;
import org.apache.xalan.utils.SystemIDResolver;

/**
 * Processor class for xsl:include markup.
 * @see <a href="http://www.w3.org/TR/xslt#dtd">XSLT DTD</a>
 * @see <a href="http://www.w3.org/TR/xslt#include">include in XSLT Specification</a>
 */
class ProcessorInclude extends XSLTElementProcessor
{
  /**
   * The base URL of the XSL document.
   * @serial
   */
  private String m_href = null;

  /**
   * Get the base identifier with which this stylesheet is associated.
   */
  public String getHref()
  {
    return m_href;
  }

  /**
   * Get the base identifier with which this stylesheet is associated.
   */
  public void setHref(String baseIdent)
  {
    m_href = baseIdent;
  }

  /**
   * Receive notification of the start of an xsl:include element.
   *
   * @param handler The calling StylesheetHandler/TemplatesBuilder.
   * @param uri The Namespace URI, or the empty string if the
   *        element has no Namespace URI or if Namespace
   *        processing is not being performed.
   * @param localName The local name (without prefix), or the
   *        empty string if Namespace processing is not being
   *        performed.
   * @param rawName The raw XML 1.0 name (with prefix), or the
   *        empty string if raw names are not available.
   * @param atts The attributes attached to the element.  If
   *        there are no attributes, it shall be an empty
   *        Attributes object.
   * @exception org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.apache.xalan.processor.StylesheetHandler#startElement
   * @see org.apache.xalan.processor.StylesheetHandler#endElement
   * @see org.xml.sax.ContentHandler#startElement
   * @see org.xml.sax.ContentHandler#endElement
   * @see org.xml.sax.Attributes
   */
  public void startElement (StylesheetHandler handler, 
                            String uri, String localName,
                            String rawName, Attributes attributes)
    throws SAXException
  {
    setPropertiesFromAttributes(handler, rawName, attributes, this);
    
    int savedStylesheetType = handler.getStylesheetType();
    handler.setStylesheetType(StylesheetHandler.STYPE_INCLUDE);

    try
    {
      parse (handler, uri, localName, rawName, attributes);
    }
    finally
    {
      handler.setStylesheetType(savedStylesheetType);
      handler.popStylesheet();
    }
  }
  
  protected void parse (StylesheetHandler handler, 
                            String uri, String localName,
                            String rawName, Attributes attributes)
    throws SAXException
  {
    URIResolver uriresolver = handler.getStylesheetProcessor().getURIResolver();

    try
    {
      XMLReader reader = null;
      boolean tryCreatingReader = true;
      EntityResolver entityResolver 
        = handler.getStylesheetProcessor().getEntityResolver();
      
      InputSource inputSource;
      if(null != entityResolver)
      {
        inputSource = entityResolver.resolveEntity(null, getHref());
        // TODO: Check for relative URL, and absolutize it???  Or no?
      }
      else
      {
        String absURL 
          = SystemIDResolver.getAbsoluteURI(getHref(), handler.getBaseIdentifier());
        inputSource = new InputSource(absURL);
      }

      if(null != uriresolver)
      {
        tryCreatingReader = false;
        reader = uriresolver.getXMLReader(inputSource);
        if(null == reader)
        {
          Node node = uriresolver.getDOMNode(inputSource);
          if(null != node)
          {
            TreeWalker walker = new TreeWalker(handler);
            walker.traverse(node);
          }
          else
            tryCreatingReader = true;
        }
      }
      if(tryCreatingReader)
      {
        reader = handler.getStylesheetProcessor().getXMLReader();
        if(null == reader)
        {
          reader = XMLReaderFactory.createXMLReader();
        }
        else
        {
          Class readerClass = ((Object)reader).getClass();
          reader = (XMLReader)readerClass.newInstance();
        }
      }
      if(null != reader)
      {
        if(null != entityResolver)
          reader.setEntityResolver(entityResolver);
        reader.setContentHandler(handler);
        reader.parse(inputSource);
      }
    }
    catch(InstantiationException ie)
    {
      handler.error("Could not clone parser!", ie);
    }
    catch(IllegalAccessException iae)
    {
      handler.error("Can not access class!", iae);
    }
    catch(IOException ioe)
    {
      handler.error(XSLTErrorResources.ER_IOEXCEPTION, new Object[] {getHref()}, ioe); 
    }
    catch(SAXException ioe)
    {
      handler.error(XSLTErrorResources.ER_IOEXCEPTION, new Object[] {getHref()}, ioe); 
    }
  }


}
