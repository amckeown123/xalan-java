package xalan2jtaglet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.swing.text.html.HTML.Tag;

import com.sun.source.doctree.DocTree;

import jdk.javadoc.doclet.*;
/**
 * Taglet for Xalan-Java documentation, giving us a standard way to
 * indicate when classes are public only because they are shared
 * across packages within Xalan code, not because they are intended for use
 * by others. Typical: "@xsl.usage internal"
 * <p>
 * Technically it might be better to OSGIfy the Xalan code, which
 * would also permit demand-loading of only the classes actually being
 * used by this execution... but that's an idea for the future.
 * <p>
 * This code renders the tag keywords (internal, advanced, experimental)
 * into their expanded renderings in the Javadoc.
 * <p>
 * Source code recreated from xalan2jtaglet.jar by IntelliJ IDEA (powered by
 * FernFlower decompiler), then adjusted to JDK 8 taglet API.
 */
public class XSLUsageTag implements Taglet {
  private static final String HEADER = "Usage:";

  public boolean inConstructor() {
    return true;
  }

  public boolean inField() {
    return true;
  }

  public boolean inMethod() {
    return true;
  }

  public boolean inOverview() {
    return true;
  }

  public boolean inPackage() {
    return true;
  }

  public boolean inType() {
    return true;
  }

  @Override
  public boolean isInlineTag() {
    return false;
  }

  @Override
  public String getName() {
    return "xsl.usage";
  }

  public String toString(Tag tag) {
    return "\n<DT><b>Usage:</b><DD>" + XSLUsage.getHTML(tag) + "</DD>\n";
  }

  public String toString(Tag[] tags) {
    if (tags == null || tags.length == 0)
      return "";

    String string = "\n<DT><b>Usage:</b><DD>";
    for (Tag tag : tags)
      string = string + XSLUsage.getHTML(tag) + ", ";

    // Remove trailing ", ", add end tag
    return string.substring(0, string.length() - 2) + "</DD>\n";
  }

  public static void register(Map tagletMap) {
    XSLUsageTag tag = new XSLUsageTag();
    Taglet t = (Taglet) tagletMap.get(tag.getName());
    if (t != null)
      tagletMap.remove(tag.getName());
    tagletMap.put(tag.getName(), tag);
  }

@Override
public Set<Location> getAllowedLocations() {
	// TODO Auto-generated method stub
	return null;
}

@Override
public String toString(List<? extends DocTree> tags, Element element) {
	// TODO Auto-generated method stub
	return null;
}
}
