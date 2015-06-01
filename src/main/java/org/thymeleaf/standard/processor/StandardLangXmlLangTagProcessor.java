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
package org.thymeleaf.standard.processor;

/**
 *
 * @author Daniel Fern&aacute;ndez
 *
 * @since 3.0.0
 *
 */
public final class StandardLangXmlLangTagProcessor extends AbstractStandardDoubleAttributeModifierTagProcessor {

    public static final int PRECEDENCE = 990;
    public static final String ATTR_NAME = "lang-xmllang";
    public static final String TARGET_ATTR_NAME_ONE = "lang";
    public static final String TARGET_ATTR_NAME_TWO = "xml:lang";


    public StandardLangXmlLangTagProcessor(final String dialectPrefix) {
        super(dialectPrefix, ATTR_NAME, PRECEDENCE, TARGET_ATTR_NAME_ONE, TARGET_ATTR_NAME_TWO, true);
    }


}