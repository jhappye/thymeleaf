/*
 * =============================================================================
 *
 *   Copyright (c) 2011-2014, The THYMELEAF team (http://www.thymeleaf.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * =============================================================================
 */
package org.thymeleaf.aurora.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.attoparser.util.TextUtil;
import org.thymeleaf.aurora.processor.IProcessor;
import org.thymeleaf.aurora.processor.element.IElementProcessor;
import org.thymeleaf.aurora.processor.node.INodeProcessor;
import org.thymeleaf.aurora.templatemode.TemplateMode;

/**
 *
 * @author Daniel Fern&aacute;ndez
 * @since 3.0.0
 *
 */
public final class AttributeDefinitions {


    // Set containing all the standard element names, for possible external reference
    public static final Set<String> ALL_STANDARD_HTML_ATTRIBUTE_NAMES;

    // Set containing all the names of the standard HTML attributes that are considered "boolean"
    private static final Set<String> ALL_STANDARD_BOOLEAN_HTML_ATTRIBUTE_NAMES;


    // We need two different repositories, for HTML and XML, because one is case-sensitive and the other is not.
    private final AttributeDefinitionRepository htmlAttributeRepository;
    private final AttributeDefinitionRepository xmlAttributeRepository;



    static {

        final List<String> htmlAttributeNameListAux =
                new ArrayList<String>(Arrays.asList(new String[]{
                        "abbr", "accept", "accept-charset", "accesskey", "action", "align", "alt", "archive", "async",
                        "autocomplete", "autofocus", "autoplay", "axis", "border", "cellpadding", "cellspacing",
                        "challenge", "char", "charoff", "charset", "checked", "cite", "class", "classid",
                        "codebase", "codetype", "cols", "colspan", "command", "content", "contenteditable",
                        "contextmenu", "controls", "coords", "data", "datetime", "declare", "default",
                        "defer", "dir", "disabled", "draggable", "dropzone", "enctype", "for", "form",
                        "formaction", "formenctype", "formmethod", "formnovalidate", "formtarget",
                        "frame", "headers", "height", "hidden", "high", "href", "hreflang", "http-equiv",
                        "icon", "id", "ismap", "keytype", "kind", "label", "lang", "list", "longdesc",
                        "loop", "low", "max", "maxlength", "media", "method", "min", "multiple", "muted",
                        "name", "nohref", "novalidate", "nowrap", "onabort", "onafterprint", "onbeforeprint",
                        "onbeforeunload", "onblur", "oncanplay", "oncanplaythrough", "onchange",
                        "onclick", "oncontextmenu", "oncuechange", "ondblclick", "ondrag", "ondragend",
                        "ondragenter", "ondragleave", "ondragover", "ondragstart", "ondrop",
                        "ondurationchange", "onemptied", "onended", "onerror", "onfocus",
                        "onformchange", "onforminput", "onhaschange", "oninput", "oninvalid", "onkeydown",
                        "onkeypress", "onkeyup", "onload", "onloadeddata", "onloadedmetadata",
                        "onloadstart", "onmessage", "onmousedown", "onmousemove", "onmouseout", "onmouseover",
                        "onmouseup", "onmousewheel", "onoffline", "ononline", "onpagehide", "onpageshow",
                        "onpause", "onplay", "onplaying", "onpopstate", "onprogress", "onratechange",
                        "onredo", "onreset", "onresize", "onscroll", "onseeked", "onseeking",
                        "onselect", "onstalled", "onstorage", "onsubmit", "onsuspend", "ontimeupdate",
                        "onundo", "onunload", "onvolumechange", "onwaiting", "open", "optimum", "pattern",
                        "placeholder", "poster", "preload", "profile", "pubdate", "radiogroup", "readonly", "rel",
                        "required", "rev", "reversed", "rows", "rowspan", "rules", "scheme", "scope", "scoped",
                        "seamless", "selected", "shape", "size", "span", "spellcheck", "src", "srclang", "standby",
                        "style", "summary", "tabindex", "title", "translate", "type", "usemap", "valign", "value",
                        "valuetype", "width", "xml:lang", "xml:space", "xmlns"
                }));

        Collections.sort(htmlAttributeNameListAux);


        ALL_STANDARD_HTML_ATTRIBUTE_NAMES =
                Collections.unmodifiableSet(new LinkedHashSet<String>(htmlAttributeNameListAux));


        final Set<String> htmlBooleanAttributeNameSetAux =
                new HashSet<String>(Arrays.asList(new String[]{
                        "async", "autofocus", "autoplay", "checked", "controls",
                        "declare", "default", "defer", "disabled", "formnovalidate",
                        "hidden", "ismap", "loop", "multiple", "novalidate",
                        "nowrap", "open", "pubdate", "readonly", "required",
                        "reversed", "selected", "scoped", "seamless"
                }));

        ALL_STANDARD_BOOLEAN_HTML_ATTRIBUTE_NAMES =
                Collections.unmodifiableSet(new LinkedHashSet<String>(htmlBooleanAttributeNameSetAux));

    }






    AttributeDefinitions(final Set<IProcessor> processors) {

        super();


        /*
         * Build the list of all Standard HTML attribute definitions
         */

        final List<HTMLAttributeDefinition> standardHTMLAttributeDefinitions =
                new ArrayList<HTMLAttributeDefinition>(ALL_STANDARD_HTML_ATTRIBUTE_NAMES.size() + 1);
        for (final String attributeNameStr : ALL_STANDARD_HTML_ATTRIBUTE_NAMES) {
            standardHTMLAttributeDefinitions.add(
                    buildHTMLAttributeDefinition(
                            AttributeNames.forHTMLName(attributeNameStr),
                            processors));
        }


        /*
         * Initialize the repositories
         */
        this.htmlAttributeRepository = new AttributeDefinitionRepository(true, processors);
        this.xmlAttributeRepository = new AttributeDefinitionRepository(false, processors);


        /*
         * Register the standard elements at the element repository, in order to initialize it
         */
        for (final AttributeDefinition attributeDefinition : standardHTMLAttributeDefinitions) {
            this.htmlAttributeRepository.storeStandardAttribute(attributeDefinition);
        }

    }










    private static HTMLAttributeDefinition buildHTMLAttributeDefinition(
            final HTMLAttributeName name, final Set<IProcessor> processors) {

        final List<IProcessor> associatedProcessorsList = new ArrayList<IProcessor>(2);
        for (final IProcessor processor : processors) {

            final TemplateMode templateMode = processor.getTemplateMode();

            if (templateMode == null) {
                throw new IllegalArgumentException("Template mode cannot be null (processor: " + processor.getClass().getName() + ")");
            }

            if (!templateMode.isHTML()) {
                // We are creating an HTML element definition, therefore we are only interested on HTML processors
                continue;
            }

            final ElementName matchingElementName;
            final AttributeName matchingAttributeName;
            if (processor instanceof IElementProcessor) {

                matchingElementName = ((IElementProcessor)processor).getMatchingElementName();
                matchingAttributeName = ((IElementProcessor)processor).getMatchingAttributeName();

            } else if (processor instanceof INodeProcessor) {

                final INodeProcessor.MatchingNodeType matchingNodeType = ((INodeProcessor)processor).getMatchingNodeType();
                if (matchingNodeType == null) {
                    throw new IllegalArgumentException("Matching node type cannot be null (processor: " + processor.getClass().getName() + ")");
                }
                if (!matchingNodeType.equals(INodeProcessor.MatchingNodeType.ELEMENT)) {
                    // We are only interested in node processors matching elements
                    continue;
                }

                matchingElementName = ((INodeProcessor)processor).getMatchingElementName();
                matchingAttributeName = ((INodeProcessor)processor).getMatchingAttributeName();

            } else {
                // Not a kind of processor we can associated with an Element Definition
                continue;
            }

            if ((matchingElementName != null && !(matchingElementName instanceof HTMLElementName)) ||
                    (matchingAttributeName != null && !(matchingAttributeName instanceof HTMLAttributeName))) {
                throw new IllegalArgumentException("HTML processors must return HTML element names and HTML attribute names (processor: " + processor.getClass().getName() + ")");
            }

            if (matchingAttributeName == null) {
                // This processor does not relate to a specific attribute - surely an element processor
                continue;
            }

            if (!matchingAttributeName.equals(name)) {
                // Doesn't match. This processor is not associated with this attribute
                continue;
            }

            associatedProcessorsList.add(processor);

        }

        // Processors associated to this element will be ordered by precedence
        Collections.sort(associatedProcessorsList, PrecedenceProcessorComparator.INSTANCE);

        // Compute whether this attribute is to be considered boolean or not
        boolean booleanAttribute = false;
        for (final String completeAttributeName : name.getCompleteAttributeNames()) {
            if (ALL_STANDARD_BOOLEAN_HTML_ATTRIBUTE_NAMES.contains(completeAttributeName)) {
                booleanAttribute = true;
            }
        }

        // Build the final instance
        return new HTMLAttributeDefinition(name, booleanAttribute, new LinkedHashSet<IProcessor>(associatedProcessorsList));

    }




    private static XMLAttributeDefinition buildXMLAttributeDefinition(
            final XMLAttributeName name, final Set<IProcessor> processors) {

        final List<IProcessor> associatedProcessorsList = new ArrayList<IProcessor>(2);
        for (final IProcessor processor : processors) {

            final TemplateMode templateMode = processor.getTemplateMode();

            if (templateMode == null) {
                throw new IllegalArgumentException("Template mode cannot be null (processor: " + processor.getClass().getName() + ")");
            }

            if (!templateMode.isXML()) {
                // We are creating an XML element definition, therefore we are only interested on XML processors
                continue;
            }

            final ElementName matchingElementName;
            final AttributeName matchingAttributeName;
            if (processor instanceof IElementProcessor) {

                matchingElementName = ((IElementProcessor)processor).getMatchingElementName();
                matchingAttributeName = ((IElementProcessor)processor).getMatchingAttributeName();

            } else if (processor instanceof INodeProcessor) {

                final INodeProcessor.MatchingNodeType matchingNodeType = ((INodeProcessor)processor).getMatchingNodeType();
                if (matchingNodeType == null) {
                    throw new IllegalArgumentException("Matching node type cannot be null (processor: " + processor.getClass().getName() + ")");
                }
                if (!matchingNodeType.equals(INodeProcessor.MatchingNodeType.ELEMENT)) {
                    // We are only interested in node processors matching elements
                    continue;
                }

                matchingElementName = ((INodeProcessor)processor).getMatchingElementName();
                matchingAttributeName = ((INodeProcessor)processor).getMatchingAttributeName();

            } else {
                // Not a kind of processor we can associated with an Element Definition
                continue;
            }

            if ((matchingElementName != null && !(matchingElementName instanceof XMLElementName)) ||
                    (matchingAttributeName != null && !(matchingAttributeName instanceof XMLAttributeName))) {
                throw new IllegalArgumentException("XML processors must return XML element names and XML attribute names (processor: " + processor.getClass().getName() + ")");
            }

            if (matchingAttributeName == null) {
                // This processor does not relate to a specific attribute - surely an element processor
                continue;
            }

            if (!matchingAttributeName.equals(name)) {
                // Doesn't match. This processor is not associated with this attribute
                continue;
            }

            associatedProcessorsList.add(processor);

        }

        // Processors associated to this element will be ordered by precedence
        Collections.sort(associatedProcessorsList, PrecedenceProcessorComparator.INSTANCE);

        // Build the final instance
        return new XMLAttributeDefinition(name, new LinkedHashSet<IProcessor>(associatedProcessorsList));

    }




    public HTMLAttributeDefinition forHTMLName(final String attributeName) {
        if (attributeName == null || attributeName.length() == 0) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        return (HTMLAttributeDefinition) this.htmlAttributeRepository.getAttribute(attributeName);
    }


    public HTMLAttributeDefinition forHTMLName(final String prefix, final String attributeName) {
        if (attributeName == null || attributeName.length() == 0) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        return (HTMLAttributeDefinition) this.htmlAttributeRepository.getAttribute(prefix, attributeName);
    }


    public HTMLAttributeDefinition forHTMLName(final char[] attributeName, final int attributeNameOffset, final int attributeNameLen) {
        if (attributeName == null || attributeNameLen == 0) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (attributeNameOffset < 0 || attributeNameLen < 0) {
            throw new IllegalArgumentException("Both name offset and length must be equal to or greater than zero");
        }
        return (HTMLAttributeDefinition) this.htmlAttributeRepository.getAttribute(attributeName, attributeNameOffset, attributeNameLen);
    }



    public XMLAttributeDefinition forXMLName(final String attributeName) {
        if (attributeName == null || attributeName.length() == 0) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        return (XMLAttributeDefinition) this.xmlAttributeRepository.getAttribute(attributeName);
    }


    public XMLAttributeDefinition forXMLName(final String prefix, final String attributeName) {
        if (attributeName == null || attributeName.length() == 0) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        return (XMLAttributeDefinition) this.xmlAttributeRepository.getAttribute(prefix, attributeName);
    }


    public XMLAttributeDefinition forXMLName(final char[] attributeName, final int attributeNameOffset, final int attributeNameLen) {
        if (attributeName == null || attributeNameLen == 0) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (attributeNameOffset < 0 || attributeNameLen < 0) {
            throw new IllegalArgumentException("Both name offset and length must be equal to or greater than zero");
        }
        return (XMLAttributeDefinition) this.xmlAttributeRepository.getAttribute(attributeName, attributeNameOffset, attributeNameLen);
    }





    /*
     * This repository class is thread-safe. The reason for this is that it not only contains the
     * standard attributes, but will also contain new instances of AttributeDefinition created during processing (created
     * when asking the repository for them when they do not exist yet). As any thread can create a new attribute,
     * this has to be lock-protected.
     */
    static final class AttributeDefinitionRepository {

        private final boolean html;

        private final Set<IProcessor> processors;

        private final List<String> standardRepositoryNames; // read-only, no sync needed
        private final List<AttributeDefinition> standardRepository; // read-only, no sync needed

        private final List<String> repositoryNames;  // read-write, sync will be needed
        private final List<AttributeDefinition> repository;  // read-write, sync will be needed

        private final ReadWriteLock lock = new ReentrantReadWriteLock(true);
        private final Lock readLock = this.lock.readLock();
        private final Lock writeLock = this.lock.writeLock();


        AttributeDefinitionRepository(final boolean html, final Set<IProcessor> processors) {

            super();

            this.html = html;
            this.processors = processors;

            this.standardRepositoryNames = (html ? new ArrayList<String>(150) : null);
            this.standardRepository = (html ? new ArrayList<AttributeDefinition>(150) : null);

            this.repositoryNames = new ArrayList<String>(500);
            this.repository = new ArrayList<AttributeDefinition>(500);

        }


        AttributeDefinition getAttribute(final char[] text, final int offset, final int len) {

            int index;

            if (this.standardRepository != null) {
                /*
                 * We first try to find it in the repository containing the standard elements, which does not need
                 * any synchronization.
                 */
                index = binarySearch(!this.html, this.standardRepositoryNames, text, offset, len);

                if (index >= 0) {
                    return this.standardRepository.get(index);
                }
            }

            /*
             * We did not find it in the repository of standard elements, so let's try in the read+write one,
             * which does require synchronization through a readwrite lock.
             */

            this.readLock.lock();
            try {

                /*
                 * First look for the element in the namespaced repository
                 */
                index = binarySearch(!this.html, this.repositoryNames, text, offset, len);

                if (index >= 0) {
                    return this.repository.get(index);
                }

            } finally {
                this.readLock.unlock();
            }


            /*
             * NOT FOUND. We need to obtain a write lock and store the text
             */
            this.writeLock.lock();
            try {
                return storeAttribute(text, offset, len);
            } finally {
                this.writeLock.unlock();
            }

        }


        AttributeDefinition getAttribute(final String completeAttributeName) {

            int index;

            if (this.standardRepository != null) {
                /*
                 * We first try to find it in the repository containing the standard elements, which does not need
                 * any synchronization.
                 */
                index = binarySearch(!this.html, this.standardRepositoryNames, completeAttributeName);

                if (index >= 0) {
                    return this.standardRepository.get(index);
                }
            }

            /*
             * We did not find it in the repository of standard elements, so let's try in the read+write one,
             * which does require synchronization through a readwrite lock.
             */

            this.readLock.lock();
            try {

                /*
                 * First look for the element in the namespaced repository
                 */
                index = binarySearch(!this.html, this.repositoryNames, completeAttributeName);

                if (index >= 0) {
                    return this.repository.get(index);
                }

            } finally {
                this.readLock.unlock();
            }


            /*
             * NOT FOUND. We need to obtain a write lock and store the text
             */
            this.writeLock.lock();
            try {
                return storeAttribute(completeAttributeName);
            } finally {
                this.writeLock.unlock();
            }

        }


        AttributeDefinition getAttribute(final String prefix, final String attributeName) {

            int index;

            if (this.standardRepository != null) {
                /*
                 * We first try to find it in the repository containing the standard elements, which does not need
                 * any synchronization.
                 */
                index = binarySearch(!this.html, this.standardRepositoryNames, prefix, attributeName);

                if (index >= 0) {
                    return this.standardRepository.get(index);
                }
            }

            /*
             * We did not find it in the repository of standard elements, so let's try in the read+write one,
             * which does require synchronization through a readwrite lock.
             */

            this.readLock.lock();
            try {

                /*
                 * First look for the element in the namespaced repository
                 */
                index = binarySearch(!this.html, this.repositoryNames, prefix, attributeName);

                if (index >= 0) {
                    return this.repository.get(index);
                }

            } finally {
                this.readLock.unlock();
            }


            /*
             * NOT FOUND. We need to obtain a write lock and store the text
             */
            this.writeLock.lock();
            try {
                return storeAttribute(prefix, attributeName);
            } finally {
                this.writeLock.unlock();
            }

        }


        private AttributeDefinition storeAttribute(final char[] text, final int offset, final int len) {

            int index = binarySearch(!this.html, this.repositoryNames, text, offset, len);
            if (index >= 0) {
                // It was already added while we were waiting for the lock!
                return this.repository.get(index);
            }

            final AttributeDefinition attributeDefinition =
                    this.html?
                            buildHTMLAttributeDefinition(AttributeNames.forHTMLName(text, offset, len), this.processors) :
                            buildXMLAttributeDefinition(AttributeNames.forXMLName(text, offset, len), this.processors);

            final String[] completeAttributeNames = attributeDefinition.attributeName.completeAttributeNames;

            for (final String completeAttributeName : completeAttributeNames) {

                index = binarySearch(!this.html, this.repositoryNames, completeAttributeName);

                // binary Search returned (-(insertion point) - 1)
                this.repositoryNames.add(((index + 1) * -1), completeAttributeName);
                this.repository.add(((index + 1) * -1), attributeDefinition);

            }

            return attributeDefinition;

        }


        private AttributeDefinition storeAttribute(final String attributeName) {

            int index = binarySearch(!this.html, this.repositoryNames, attributeName);
            if (index >= 0) {
                // It was already added while we were waiting for the lock!
                return this.repository.get(index);
            }

            final AttributeDefinition attributeDefinition =
                    this.html?
                            buildHTMLAttributeDefinition(AttributeNames.forHTMLName(attributeName), this.processors) :
                            buildXMLAttributeDefinition(AttributeNames.forXMLName(attributeName), this.processors);

            final String[] completeAttributeNames = attributeDefinition.attributeName.completeAttributeNames;

            for (final String completeAttributeName : completeAttributeNames) {

                index = binarySearch(!this.html, this.repositoryNames, completeAttributeName);

                // binary Search returned (-(insertion point) - 1)
                this.repositoryNames.add(((index + 1) * -1), completeAttributeName);
                this.repository.add(((index + 1) * -1), attributeDefinition);

            }

            return attributeDefinition;

        }


        private AttributeDefinition storeAttribute(final String prefix, final String attributeName) {

            int index = binarySearch(!this.html, this.repositoryNames, prefix, attributeName);
            if (index >= 0) {
                // It was already added while we were waiting for the lock!
                return this.repository.get(index);
            }

            final AttributeDefinition attributeDefinition =
                    this.html?
                            buildHTMLAttributeDefinition(AttributeNames.forHTMLName(prefix, attributeName), this.processors) :
                            buildXMLAttributeDefinition(AttributeNames.forXMLName(prefix, attributeName), this.processors);

            final String[] completeAttributeNames = attributeDefinition.attributeName.completeAttributeNames;

            for (final String completeAttributeName : completeAttributeNames) {

                index = binarySearch(!this.html, this.repositoryNames, completeAttributeName);

                // binary Search returned (-(insertion point) - 1)
                this.repositoryNames.add(((index + 1) * -1), completeAttributeName);
                this.repository.add(((index + 1) * -1), attributeDefinition);

            }

            return attributeDefinition;

        }


        private AttributeDefinition storeStandardAttribute(final AttributeDefinition attributeDefinition) {

            // This method will only be called from within the AttributeDefinitions class itself, during initialization of
            // standard elements.

            final String[] completeAttributeNames = attributeDefinition.attributeName.completeAttributeNames;

            int index;
            for (final String completeAttributeName : completeAttributeNames) {

                index = binarySearch(!this.html, this.standardRepositoryNames, completeAttributeName);

                // binary Search returned (-(insertion point) - 1)
                this.standardRepositoryNames.add(((index + 1) * -1), completeAttributeName);
                this.standardRepository.add(((index + 1) * -1), attributeDefinition);

                index = binarySearch(!this.html, this.repositoryNames, completeAttributeName);

                // binary Search returned (-(insertion point) - 1)
                this.repositoryNames.add(((index + 1) * -1), completeAttributeName);
                this.repository.add(((index + 1) * -1), attributeDefinition);

            }

            return attributeDefinition;

        }


        private static int binarySearch(
                final boolean caseSensitive, final List<String> values, final char[] text, final int offset, final int len) {

            int low = 0;
            int high = values.size() - 1;

            int mid, cmp;
            String midVal;

            while (low <= high) {

                mid = (low + high) >>> 1;
                midVal = values.get(mid);

                cmp = TextUtil.compareTo(caseSensitive, midVal, 0, midVal.length(), text, offset, len);

                if (cmp < 0) {
                    low = mid + 1;
                } else if (cmp > 0) {
                    high = mid - 1;
                } else {
                    // Found!!
                    return mid;
                }

            }

            return -(low + 1);  // Not Found!! We return (-(insertion point) - 1), to guarantee all non-founds are < 0

        }


        private static int binarySearch(final boolean caseSensitive, final List<String> values, final String text) {

            int low = 0;
            int high = values.size() - 1;

            int mid, cmp;
            String midVal;

            while (low <= high) {

                mid = (low + high) >>> 1;
                midVal = values.get(mid);

                cmp = TextUtil.compareTo(caseSensitive, midVal, text);

                if (cmp < 0) {
                    low = mid + 1;
                } else if (cmp > 0) {
                    high = mid - 1;
                } else {
                    // Found!!
                    return mid;
                }

            }

            return -(low + 1);  // Not Found!! We return (-(insertion point) - 1), to guarantee all non-founds are < 0

        }


        private static int binarySearch(final boolean caseSensitive,
                                        final List<String> values, final String prefix, final String attributeName) {

            // This method will be specialized in finding prefixed attribute names (in the prefix:name form)

            if (prefix == null) {
                return binarySearch(caseSensitive, values, attributeName);
            }

            final int prefixLen = prefix.length();
            final int attributeNameLen = attributeName.length();

            int low = 0;
            int high = values.size() - 1;

            int mid, cmp;
            String midVal;
            int midValLen;

            while (low <= high) {

                mid = (low + high) >>> 1;
                midVal = values.get(mid);
                midValLen = midVal.length();

                if (TextUtil.startsWith(caseSensitive, midVal, prefix)) {

                    // Prefix matched, but it could be a mere coincidence if the text being evaluated doesn't have
                    // a ':' after the prefix letters, so we will make sure by comparing the next char manually

                    if (midValLen <= prefixLen) {
                        // midVal is exactly as prefix, therefore it goes first

                        low = mid + 1;

                    } else {

                        // Compare the next char
                        cmp = midVal.charAt(prefixLen) - ':';

                        if (cmp < 0) {
                            low = mid + 1;
                        } else if (cmp > 0) {
                            high = mid - 1;
                        } else {

                            // Prefix matches and we made sure midVal has a ':', so let's try the attributeName
                            cmp = TextUtil.compareTo(caseSensitive, midVal, prefixLen + 1, (midValLen - (prefixLen + 1)), attributeName, 0, attributeNameLen);

                            if (cmp < 0) {
                                low = mid + 1;
                            } else if (cmp > 0) {
                                high = mid - 1;
                            } else {
                                // Found!!
                                return mid;
                            }

                        }

                    }

                } else {

                    // midVal does not start with prefix, so comparing midVal and prefix should be enough

                    cmp = TextUtil.compareTo(caseSensitive, midVal, prefix);

                    if (cmp < 0) {
                        low = mid + 1;
                    } else if (cmp > 0) {
                        high = mid - 1;
                    } else {
                        // This is impossible - if they were the same, we'd have detected it already!
                        throw new IllegalStateException("Bad comparison of midVal \"" + midVal + "\" and prefix \"" + prefix + "\"");
                    }

                }

            }

            return -(low + 1);  // Not Found!! We return (-(insertion point) - 1), to guarantee all non-founds are < 0

        }


    }



}