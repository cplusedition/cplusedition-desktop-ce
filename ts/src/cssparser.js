/*!            
    C+edition for Desktop, Community Edition.
    Copyright (C) 2021 Cplusedition Limited.  All rights reserved.
    
    The author licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
"use strict";
var
XxXPo
        = (function () {
            var CSSIssueType = /** @class */ (function () {
                function CSSIssueType(id, message) {
                    this.id = id;
                    this.message = message;
                }
                return CSSIssueType;
            }());
            function localize(key, value) {
                return value;
            }
    /* export */ var ParseError = {
                NumberExpected: new CSSIssueType('css-numberexpected', localize('expected.number', "number expected")),
                ConditionExpected: new CSSIssueType('css-conditionexpected', localize('expected.condt', "condition expected")),
                RuleOrSelectorExpected: new CSSIssueType('css-ruleorselectorexpected', localize('expected.ruleorselector', "at-rule or selector expected")),
                DotExpected: new CSSIssueType('css-dotexpected', localize('expected.dot', "dot expected")),
                ColonExpected: new CSSIssueType('css-colonexpected', localize('expected.colon', "colon expected")),
                SemiColonExpected: new CSSIssueType('css-semicolonexpected', localize('expected.semicolon', "semi-colon expected")),
                TermExpected: new CSSIssueType('css-termexpected', localize('expected.term', "term expected")),
                ExpressionExpected: new CSSIssueType('css-expressionexpected', localize('expected.expression', "expression expected")),
                OperatorExpected: new CSSIssueType('css-operatorexpected', localize('expected.operator', "operator expected")),
                IdentifierExpected: new CSSIssueType('css-identifierexpected', localize('expected.ident', "identifier expected")),
                PercentageExpected: new CSSIssueType('css-percentageexpected', localize('expected.percentage', "percentage expected")),
                URIOrStringExpected: new CSSIssueType('css-uriorstringexpected', localize('expected.uriorstring', "uri or string expected")),
                URIExpected: new CSSIssueType('css-uriexpected', localize('expected.uri', "URI expected")),
                VariableNameExpected: new CSSIssueType('css-varnameexpected', localize('expected.varname', "variable name expected")),
                VariableValueExpected: new CSSIssueType('css-varvalueexpected', localize('expected.varvalue', "variable value expected")),
                PropertyValueExpected: new CSSIssueType('css-propertyvalueexpected', localize('expected.propvalue', "property value expected")),
                LeftCurlyExpected: new CSSIssueType('css-lcurlyexpected', localize('expected.lcurly', "{ expected")),
                RightCurlyExpected: new CSSIssueType('css-rcurlyexpected', localize('expected.rcurly', "} expected")),
                LeftSquareBracketExpected: new CSSIssueType('css-rbracketexpected', localize('expected.lsquare', "[ expected")),
                RightSquareBracketExpected: new CSSIssueType('css-lbracketexpected', localize('expected.rsquare', "] expected")),
                LeftParenthesisExpected: new CSSIssueType('css-lparentexpected', localize('expected.lparen', "( expected")),
                RightParenthesisExpected: new CSSIssueType('css-rparentexpected', localize('expected.rparent', ") expected")),
                CommaExpected: new CSSIssueType('css-commaexpected', localize('expected.comma', "comma expected")),
                PageDirectiveOrDeclarationExpected: new CSSIssueType('css-pagedirordeclexpected', localize('expected.pagedirordecl', "page directive or declaraton expected")),
                UnknownAtRule: new CSSIssueType('css-unknownatrule', localize('unknown.atrule', "at-rule unknown")),
                UnknownKeyword: new CSSIssueType('css-unknownkeyword', localize('unknown.keyword', "unknown keyword")),
                SelectorExpected: new CSSIssueType('css-selectorexpected', localize('expected.selector', "selector expected")),
                StringLiteralExpected: new CSSIssueType('css-stringliteralexpected', localize('expected.stringliteral', "string literal expected")),
                WhitespaceExpected: new CSSIssueType('css-whitespaceexpected', localize('expected.whitespace', "whitespace expected")),
                MediaQueryExpected: new CSSIssueType('css-mediaqueryexpected', localize('expected.mediaquery', "media query expected"))
            };
            var __extends = (this && this.__extends) || (function () {
                var extendStatics = function (d, b) {
                    extendStatics = Object.setPrototypeOf ||
                        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
                        function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
                    return extendStatics(d, b);
                };
                return function (d, b) {
                    extendStatics(d, b);
                    function __() { this.constructor = d; }
                    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
                };
            })();
            /// <summary>
            /// Nodes for the css 2.1 specification. See for reference:
            /// http://www.w3.org/TR/CSS21/grammar.html#grammar
            /// </summary>
            var NodeType;
            (function (NodeType) {
                NodeType.Undefined = 0;
                NodeType.Identifier = 1;
                NodeType.Stylesheet = 2;
                NodeType.Ruleset = 3;
                NodeType.Selector = 4;
                NodeType.SimpleSelector = 5;
                NodeType.SelectorInterpolation = 6;
                NodeType.SelectorCombinator = 7;
                NodeType.SelectorCombinatorParent = 8;
                NodeType.SelectorCombinatorSibling = 9;
                NodeType.SelectorCombinatorAllSiblings = 10;
                NodeType.SelectorCombinatorShadowPiercingDescendant = 11;
                NodeType.Page = 12;
                NodeType.PageBoxMarginBox = 13;
                NodeType.ClassSelector = 14;
                NodeType.IdentifierSelector = 15;
                NodeType.ElementNameSelector = 16;
                NodeType.PseudoSelector = 17;
                NodeType.AttributeSelector = 18;
                NodeType.Declaration = 19;
                NodeType.Declarations = 20;
                NodeType.Property = 21;
                NodeType.Expression = 22;
                NodeType.BinaryExpression = 23;
                NodeType.Term = 24;
                NodeType.Operator = 25;
                NodeType.Value = 26;
                NodeType.StringLiteral = 27;
                NodeType.URILiteral = 28;
                NodeType.EscapedValue = 29;
                NodeType.Function = 30;
                NodeType.NumericValue = 31;
                NodeType.HexColorValue = 32;
                NodeType.MixinDeclaration = 33;
                NodeType.MixinReference = 34;
                NodeType.VariableName = 35;
                NodeType.VariableDeclaration = 36;
                NodeType.Prio = 37;
                NodeType.Interpolation = 38;
                NodeType.NestedProperties = 39;
                NodeType.ExtendsReference = 40;
                NodeType.SelectorPlaceholder = 41;
                NodeType.Debug = 42;
                NodeType.If = 43;
                NodeType.Else = 44;
                NodeType.For = 45;
                NodeType.Each = 46;
                NodeType.While = 47;
                NodeType.MixinContent = 48;
                NodeType.Media = 49;
                NodeType.Keyframe = 50;
                NodeType.FontFace = 51;
                NodeType.Import = 52;
                NodeType.Namespace = 53;
                NodeType.Invocation = 54;
                NodeType.FunctionDeclaration = 55;
                NodeType.ReturnStatement = 56;
                NodeType.MediaQuery = 57;
                NodeType.FunctionParameter = 58;
                NodeType.FunctionArgument = 59;
                NodeType.KeyframeSelector = 60;
                NodeType.ViewPort = 61;
                NodeType.Document = 62;
                NodeType.AtApplyRule = 63;
                NodeType.CustomPropertyDeclaration = 64;
                NodeType.CustomPropertySet = 65;
                NodeType.ListEntry = 66;
                NodeType.Supports = 67;
                NodeType.SupportsCondition = 68;
                NodeType.NamespacePrefix = 69;
                NodeType.GridLine = 70;
                NodeType.Plugin = 71;
                NodeType.UnknownAtRule = 72;

            })(NodeType || (NodeType = {}));
            var ReferenceType;
            (function (ReferenceType) {
                ReferenceType[ReferenceType["Mixin"] = 0] = "Mixin";
                ReferenceType[ReferenceType["Rule"] = 1] = "Rule";
                ReferenceType[ReferenceType["Variable"] = 2] = "Variable";
                ReferenceType[ReferenceType["Function"] = 3] = "Function";
                ReferenceType[ReferenceType["Keyframe"] = 4] = "Keyframe";
                ReferenceType[ReferenceType["Unknown"] = 5] = "Unknown";
            })(ReferenceType || (ReferenceType = {}));
    /* export */ function getNodeAtOffset(node, offset) {
                var candidate = null;
                if (!node || offset < node.offset || offset > node.end) {
                    return null;
                }
                node.accept(function (node) {
                    if (node.offset === -1 && node.length === -1) {
                        return true;
                    }
                    if (node.offset <= offset && node.end >= offset) {
                        if (!candidate) {
                            candidate = node;
                        }
                        else if (node.length <= candidate.length) {
                            candidate = node;
                        }
                        return true;
                    }
                    return false;
                });
                return candidate;
            }
    /* export */ function getNodePath(node, offset) {
                var candidate = getNodeAtOffset(node, offset);
                var path = [];
                while (candidate) {
                    path.unshift(candidate);
                    candidate = candidate.parent;
                }
                return path;
            }
    /* export */ function getParentDeclaration(node) {
                var decl = node.findParent(NodeType.Declaration);
                if (decl && decl.getValue() && decl.getValue().encloses(node)) {
                    return decl;
                }
                return null;
            }
            var Node = /** @class */ (function () {
                function Node(offset, len, nodeType) {
                    if (offset === void 0) { offset = -1; }
                    if (len === void 0) { len = -1; }
                    this.parent = null;
                    this.offset = offset;
                    this.length = len;
                    if (nodeType) {
                        this.nodeType = nodeType;
                    }
                }
                Object.defineProperty(Node.prototype, "end", {
                    get: function () { return this.offset + this.length; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(Node.prototype, "type", {
                    get: function () {
                        return this.nodeType || NodeType.Undefined;
                    },
                    set: function (type) {
                        this.nodeType = type;
                    },
                    enumerable: true,
                    configurable: true
                });
                Node.prototype.getTextProvider = function () {
                    var node = this;
                    while (node && !node.textProvider) {
                        node = node.parent;
                    }
                    if (node) {
                        return node.textProvider;
                    }
                    return function () { return 'unknown'; };
                };
                Node.prototype.getText = function () {
                    return this.getTextProvider()(this.offset, this.length);
                };
                Node.prototype.matches = function (str) {
                    return this.length === str.length && this.getTextProvider()(this.offset, this.length) === str;
                };
                Node.prototype.startsWith = function (str) {
                    return this.length >= str.length && this.getTextProvider()(this.offset, str.length) === str;
                };
                Node.prototype.endsWith = function (str) {
                    return this.length >= str.length && this.getTextProvider()(this.end - str.length, str.length) === str;
                };
                Node.prototype.accept = function (visitor) {
                    if (visitor(this) && this.children) {
                        for (var _i = 0, _a = this.children; _i < _a.length; _i++) {
                            var child = _a[_i];
                            child.accept(visitor);
                        }
                    }
                };
                Node.prototype.acceptVisitor = function (visitor) {
                    this.accept(visitor.visitNode.bind(visitor));
                };
                Node.prototype.adoptChild = function (node, index) {
                    if (index === void 0) { index = -1; }
                    if (node.parent && node.parent.children) {
                        var idx = node.parent.children.indexOf(node);
                        if (idx >= 0) {
                            node.parent.children.splice(idx, 1);
                        }
                    }
                    node.parent = this;
                    var children = this.children;
                    if (!children) {
                        children = this.children = [];
                    }
                    if (index !== -1) {
                        children.splice(index, 0, node);
                    }
                    else {
                        children.push(node);
                    }
                    return node;
                };
                Node.prototype.attachTo = function (parent, index) {
                    if (index === void 0) { index = -1; }
                    if (parent) {
                        parent.adoptChild(this, index);
                    }
                    return this;
                };
                Node.prototype.collectIssues = function (results) {
                    if (this.issues) {
                        results.push.apply(results, this.issues);
                    }
                };
                Node.prototype.addIssue = function (issue) {
                    if (!this.issues) {
                        this.issues = [];
                    }
                    this.issues.push(issue);
                };
                Node.prototype.hasIssue = function (rule) {
                    return Array.isArray(this.issues) && this.issues.some(function (i) { return i.getRule() === rule; });
                };
                Node.prototype.isErroneous = function (recursive) {
                    if (recursive === void 0) { recursive = false; }
                    if (this.issues && this.issues.length > 0) {
                        return true;
                    }
                    return recursive && Array.isArray(this.children) && this.children.some(function (c) { return c.isErroneous(true); });
                };
                Node.prototype.setNode = function (field, node, index) {
                    if (index === void 0) { index = -1; }
                    if (node) {
                        node.attachTo(this, index);
                        this[field] = node;
                        return true;
                    }
                    return false;
                };
                Node.prototype.addChild = function (node) {
                    if (node) {
                        if (!this.children) {
                            this.children = [];
                        }
                        node.attachTo(this);
                        this.updateOffsetAndLength(node);
                        return true;
                    }
                    return false;
                };
                Node.prototype.updateOffsetAndLength = function (node) {
                    if (node.offset < this.offset || this.offset === -1) {
                        this.offset = node.offset;
                    }
                    var nodeEnd = node.end;
                    if ((nodeEnd > this.end) || this.length === -1) {
                        this.length = nodeEnd - this.offset;
                    }
                };
                Node.prototype.hasChildren = function () {
                    return this.children && this.children.length > 0;
                };
                Node.prototype.getChildren = function () {
                    return this.children ? this.children.slice(0) : [];
                };
                Node.prototype.getChild = function (index) {
                    if (this.children && index < this.children.length) {
                        return this.children[index];
                    }
                    return null;
                };
                Node.prototype.addChildren = function (nodes) {
                    for (var _i = 0, nodes_1 = nodes; _i < nodes_1.length; _i++) {
                        var node = nodes_1[_i];
                        this.addChild(node);
                    }
                };
                Node.prototype.findFirstChildBeforeOffset = function (offset) {
                    if (this.children) {
                        var current = null;
                        for (var i = this.children.length - 1; i >= 0; i--) {
                            current = this.children[i];
                            if (current.offset <= offset) {
                                return current;
                            }
                        }
                    }
                    return null;
                };
                Node.prototype.findChildAtOffset = function (offset, goDeep) {
                    var current = this.findFirstChildBeforeOffset(offset);
                    if (current && current.end >= offset) {
                        if (goDeep) {
                            return current.findChildAtOffset(offset, true) || current;
                        }
                        return current;
                    }
                    return null;
                };
                Node.prototype.encloses = function (candidate) {
                    return this.offset <= candidate.offset && this.offset + this.length >= candidate.offset + candidate.length;
                };
                Node.prototype.getParent = function () {
                    var result = this.parent;
                    while (result instanceof Nodelist) {
                        result = result.parent;
                    }
                    return result;
                };
                Node.prototype.findParent = function (type) {
                    var result = this;
                    while (result && result.type !== type) {
                        result = result.parent;
                    }
                    return result;
                };
                Node.prototype.findAParent = function () {
                    var types = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        types[_i] = arguments[_i];
                    }
                    var result = this;
                    while (result && !types.some(function (t) { return result.type === t; })) {
                        result = result.parent;
                    }
                    return result;
                };
                Node.prototype.setData = function (key, value) {
                    if (!this.options) {
                        this.options = {};
                    }
                    this.options[key] = value;
                };
                Node.prototype.getData = function (key) {
                    if (!this.options || !this.options.hasOwnProperty(key)) {
                        return null;
                    }
                    return this.options[key];
                };
                return Node;
            }());
            var Nodelist = /** @class */ (function (_super) {
                __extends(Nodelist, _super);
                function Nodelist(parent, index) {
                    if (index === void 0) { index = -1; }
                    var _this = _super.call(this, -1, -1) || this;
                    _this.attachTo(parent, index);
                    _this.offset = -1;
                    _this.length = -1;
                    return _this;
                }
                return Nodelist;
            }(Node));
            var Identifier = /** @class */ (function (_super) {
                __extends(Identifier, _super);
                function Identifier(offset, length) {
                    var _this = _super.call(this, offset, length) || this;
                    _this.isCustomProperty = false;
                    return _this;
                }
                Object.defineProperty(Identifier.prototype, "type", {
                    get: function () {
                        return NodeType.Identifier;
                    },
                    enumerable: true,
                    configurable: true
                });
                Identifier.prototype.containsInterpolation = function () {
                    return this.hasChildren();
                };
                return Identifier;
            }(Node));
            var Stylesheet = /** @class */ (function (_super) {
                __extends(Stylesheet, _super);
                function Stylesheet(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(Stylesheet.prototype, "type", {
                    get: function () {
                        return NodeType.Stylesheet;
                    },
                    enumerable: true,
                    configurable: true
                });
                Stylesheet.prototype.setName = function (value) {
                    this.name = value;
                };
                return Stylesheet;
            }(Node));
            var Declarations = /** @class */ (function (_super) {
                __extends(Declarations, _super);
                function Declarations(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(Declarations.prototype, "type", {
                    get: function () {
                        return NodeType.Declarations;
                    },
                    enumerable: true,
                    configurable: true
                });
                return Declarations;
            }(Node));
            var BodyDeclaration = /** @class */ (function (_super) {
                __extends(BodyDeclaration, _super);
                function BodyDeclaration(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                BodyDeclaration.prototype.getDeclarations = function () {
                    return this.declarations;
                };
                BodyDeclaration.prototype.setDeclarations = function (decls) {
                    return this.setNode('declarations', decls);
                };
                return BodyDeclaration;
            }(Node));
            var RuleSet = /** @class */ (function (_super) {
                __extends(RuleSet, _super);
                function RuleSet(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(RuleSet.prototype, "type", {
                    get: function () {
                        return NodeType.Ruleset;
                    },
                    enumerable: true,
                    configurable: true
                });
                RuleSet.prototype.getSelectors = function () {
                    if (!this.selectors) {
                        this.selectors = new Nodelist(this);
                    }
                    return this.selectors;
                };
                RuleSet.prototype.isNested = function () {
                    return !!this.parent && this.parent.findParent(NodeType.Declarations) !== null;
                };
                return RuleSet;
            }(BodyDeclaration));
            var Selector = /** @class */ (function (_super) {
                __extends(Selector, _super);
                function Selector(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(Selector.prototype, "type", {
                    get: function () {
                        return NodeType.Selector;
                    },
                    enumerable: true,
                    configurable: true
                });
                return Selector;
            }(Node));
            var SimpleSelector = /** @class */ (function (_super) {
                __extends(SimpleSelector, _super);
                function SimpleSelector(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(SimpleSelector.prototype, "type", {
                    get: function () {
                        return NodeType.SimpleSelector;
                    },
                    enumerable: true,
                    configurable: true
                });
                return SimpleSelector;
            }(Node));
            var AtApplyRule = /** @class */ (function (_super) {
                __extends(AtApplyRule, _super);
                function AtApplyRule(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(AtApplyRule.prototype, "type", {
                    get: function () {
                        return NodeType.AtApplyRule;
                    },
                    enumerable: true,
                    configurable: true
                });
                AtApplyRule.prototype.setIdentifier = function (node) {
                    return this.setNode('identifier', node, 0);
                };
                AtApplyRule.prototype.getIdentifier = function () {
                    return this.identifier;
                };
                AtApplyRule.prototype.getName = function () {
                    return this.identifier ? this.identifier.getText() : '';
                };
                return AtApplyRule;
            }(Node));
            var AbstractDeclaration = /** @class */ (function (_super) {
                __extends(AbstractDeclaration, _super);
                function AbstractDeclaration(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                return AbstractDeclaration;
            }(Node));
            var CustomPropertyDeclaration = /** @class */ (function (_super) {
                __extends(CustomPropertyDeclaration, _super);
                function CustomPropertyDeclaration(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(CustomPropertyDeclaration.prototype, "type", {
                    get: function () {
                        return NodeType.CustomPropertyDeclaration;
                    },
                    enumerable: true,
                    configurable: true
                });
                CustomPropertyDeclaration.prototype.setProperty = function (node) {
                    return this.setNode('property', node);
                };
                CustomPropertyDeclaration.prototype.getProperty = function () {
                    return this.property;
                };
                CustomPropertyDeclaration.prototype.setValue = function (value) {
                    return this.setNode('value', value);
                };
                CustomPropertyDeclaration.prototype.getValue = function () {
                    return this.value;
                };
                CustomPropertyDeclaration.prototype.setPropertySet = function (value) {
                    return this.setNode('propertySet', value);
                };
                CustomPropertyDeclaration.prototype.getPropertySet = function () {
                    return this.propertySet;
                };
                return CustomPropertyDeclaration;
            }(AbstractDeclaration));
            var CustomPropertySet = /** @class */ (function (_super) {
                __extends(CustomPropertySet, _super);
                function CustomPropertySet(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(CustomPropertySet.prototype, "type", {
                    get: function () {
                        return NodeType.CustomPropertySet;
                    },
                    enumerable: true,
                    configurable: true
                });
                return CustomPropertySet;
            }(BodyDeclaration));
            var Declaration = /** @class */ (function (_super) {
                __extends(Declaration, _super);
                function Declaration(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(Declaration.prototype, "type", {
                    get: function () {
                        return NodeType.Declaration;
                    },
                    enumerable: true,
                    configurable: true
                });
                Declaration.prototype.setProperty = function (node) {
                    return this.setNode('property', node);
                };
                Declaration.prototype.getProperty = function () {
                    return this.property;
                };
                Declaration.prototype.getFullPropertyName = function () {
                    var propertyName = this.property ? this.property.getName() : 'unknown';
                    if (this.parent instanceof Declarations && this.parent.getParent() instanceof NestedProperties) {
                        var parentDecl = this.parent.getParent().getParent();
                        if (parentDecl instanceof Declaration) {
                            return parentDecl.getFullPropertyName() + propertyName;
                        }
                    }
                    return propertyName;
                };
                Declaration.prototype.getNonPrefixedPropertyName = function () {
                    var propertyName = this.getFullPropertyName();
                    if (propertyName && propertyName.charAt(0) === '-') {
                        var vendorPrefixEnd = propertyName.indexOf('-', 1);
                        if (vendorPrefixEnd !== -1) {
                            return propertyName.substring(vendorPrefixEnd + 1);
                        }
                    }
                    return propertyName;
                };
                Declaration.prototype.setValue = function (value) {
                    return this.setNode('value', value);
                };
                Declaration.prototype.getValue = function () {
                    return this.value;
                };
                Declaration.prototype.setNestedProperties = function (value) {
                    return this.setNode('nestedProperties', value);
                };
                Declaration.prototype.getNestedProperties = function () {
                    return this.nestedProperties;
                };
                return Declaration;
            }(AbstractDeclaration));
            var Property = /** @class */ (function (_super) {
                __extends(Property, _super);
                function Property(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(Property.prototype, "type", {
                    get: function () {
                        return NodeType.Property;
                    },
                    enumerable: true,
                    configurable: true
                });
                Property.prototype.setIdentifier = function (value) {
                    return this.setNode('identifier', value);
                };
                Property.prototype.getIdentifier = function () {
                    return this.identifier;
                };
                Property.prototype.getName = function () {
                    return this.getText();
                };
                Property.prototype.isCustomProperty = function () {
                    return this.identifier.isCustomProperty;
                };
                return Property;
            }(Node));
            var Invocation = /** @class */ (function (_super) {
                __extends(Invocation, _super);
                function Invocation(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(Invocation.prototype, "type", {
                    get: function () {
                        return NodeType.Invocation;
                    },
                    enumerable: true,
                    configurable: true
                });
                Invocation.prototype.getArguments = function () {
                    if (!this.arguments) {
                        this.arguments = new Nodelist(this);
                    }
                    return this.arguments;
                };
                return Invocation;
            }(Node));
            var Function = /** @class */ (function (_super) {
                __extends(Function, _super);
                function Function(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(Function.prototype, "type", {
                    get: function () {
                        return NodeType.Function;
                    },
                    enumerable: true,
                    configurable: true
                });
                Function.prototype.setIdentifier = function (node) {
                    return this.setNode('identifier', node, 0);
                };
                Function.prototype.getIdentifier = function () {
                    return this.identifier;
                };
                Function.prototype.getName = function () {
                    return this.identifier ? this.identifier.getText() : '';
                };
                return Function;
            }(Invocation));
            var FunctionParameter = /** @class */ (function (_super) {
                __extends(FunctionParameter, _super);
                function FunctionParameter(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(FunctionParameter.prototype, "type", {
                    get: function () {
                        return NodeType.FunctionParameter;
                    },
                    enumerable: true,
                    configurable: true
                });
                FunctionParameter.prototype.setIdentifier = function (node) {
                    return this.setNode('identifier', node, 0);
                };
                FunctionParameter.prototype.getIdentifier = function () {
                    return this.identifier;
                };
                FunctionParameter.prototype.getName = function () {
                    return this.identifier ? this.identifier.getText() : '';
                };
                FunctionParameter.prototype.setDefaultValue = function (node) {
                    return this.setNode('defaultValue', node, 0);
                };
                FunctionParameter.prototype.getDefaultValue = function () {
                    return this.defaultValue;
                };
                return FunctionParameter;
            }(Node));
            var FunctionArgument = /** @class */ (function (_super) {
                __extends(FunctionArgument, _super);
                function FunctionArgument(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(FunctionArgument.prototype, "type", {
                    get: function () {
                        return NodeType.FunctionArgument;
                    },
                    enumerable: true,
                    configurable: true
                });
                FunctionArgument.prototype.setIdentifier = function (node) {
                    return this.setNode('identifier', node, 0);
                };
                FunctionArgument.prototype.getIdentifier = function () {
                    return this.identifier;
                };
                FunctionArgument.prototype.getName = function () {
                    return this.identifier ? this.identifier.getText() : '';
                };
                FunctionArgument.prototype.setValue = function (node) {
                    return this.setNode('value', node, 0);
                };
                FunctionArgument.prototype.getValue = function () {
                    return this.value;
                };
                return FunctionArgument;
            }(Node));
            var IfStatement = /** @class */ (function (_super) {
                __extends(IfStatement, _super);
                function IfStatement(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(IfStatement.prototype, "type", {
                    get: function () {
                        return NodeType.If;
                    },
                    enumerable: true,
                    configurable: true
                });
                IfStatement.prototype.setExpression = function (node) {
                    return this.setNode('expression', node, 0);
                };
                IfStatement.prototype.setElseClause = function (elseClause) {
                    return this.setNode('elseClause', elseClause);
                };
                return IfStatement;
            }(BodyDeclaration));
            var ForStatement = /** @class */ (function (_super) {
                __extends(ForStatement, _super);
                function ForStatement(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(ForStatement.prototype, "type", {
                    get: function () {
                        return NodeType.For;
                    },
                    enumerable: true,
                    configurable: true
                });
                ForStatement.prototype.setVariable = function (node) {
                    return this.setNode('variable', node, 0);
                };
                return ForStatement;
            }(BodyDeclaration));
            var EachStatement = /** @class */ (function (_super) {
                __extends(EachStatement, _super);
                function EachStatement(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(EachStatement.prototype, "type", {
                    get: function () {
                        return NodeType.Each;
                    },
                    enumerable: true,
                    configurable: true
                });
                EachStatement.prototype.getVariables = function () {
                    if (!this.variables) {
                        this.variables = new Nodelist(this);
                    }
                    return this.variables;
                };
                return EachStatement;
            }(BodyDeclaration));
            var WhileStatement = /** @class */ (function (_super) {
                __extends(WhileStatement, _super);
                function WhileStatement(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(WhileStatement.prototype, "type", {
                    get: function () {
                        return NodeType.While;
                    },
                    enumerable: true,
                    configurable: true
                });
                return WhileStatement;
            }(BodyDeclaration));
            var ElseStatement = /** @class */ (function (_super) {
                __extends(ElseStatement, _super);
                function ElseStatement(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(ElseStatement.prototype, "type", {
                    get: function () {
                        return NodeType.Else;
                    },
                    enumerable: true,
                    configurable: true
                });
                return ElseStatement;
            }(BodyDeclaration));
            var FunctionDeclaration = /** @class */ (function (_super) {
                __extends(FunctionDeclaration, _super);
                function FunctionDeclaration(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(FunctionDeclaration.prototype, "type", {
                    get: function () {
                        return NodeType.FunctionDeclaration;
                    },
                    enumerable: true,
                    configurable: true
                });
                FunctionDeclaration.prototype.setIdentifier = function (node) {
                    return this.setNode('identifier', node, 0);
                };
                FunctionDeclaration.prototype.getIdentifier = function () {
                    return this.identifier;
                };
                FunctionDeclaration.prototype.getName = function () {
                    return this.identifier ? this.identifier.getText() : '';
                };
                FunctionDeclaration.prototype.getParameters = function () {
                    if (!this.parameters) {
                        this.parameters = new Nodelist(this);
                    }
                    return this.parameters;
                };
                return FunctionDeclaration;
            }(BodyDeclaration));
            var ViewPort = /** @class */ (function (_super) {
                __extends(ViewPort, _super);
                function ViewPort(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(ViewPort.prototype, "type", {
                    get: function () {
                        return NodeType.ViewPort;
                    },
                    enumerable: true,
                    configurable: true
                });
                return ViewPort;
            }(BodyDeclaration));
            var FontFace = /** @class */ (function (_super) {
                __extends(FontFace, _super);
                function FontFace(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(FontFace.prototype, "type", {
                    get: function () {
                        return NodeType.FontFace;
                    },
                    enumerable: true,
                    configurable: true
                });
                return FontFace;
            }(BodyDeclaration));
            var NestedProperties = /** @class */ (function (_super) {
                __extends(NestedProperties, _super);
                function NestedProperties(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(NestedProperties.prototype, "type", {
                    get: function () {
                        return NodeType.NestedProperties;
                    },
                    enumerable: true,
                    configurable: true
                });
                return NestedProperties;
            }(BodyDeclaration));
            var Keyframe = /** @class */ (function (_super) {
                __extends(Keyframe, _super);
                function Keyframe(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(Keyframe.prototype, "type", {
                    get: function () {
                        return NodeType.Keyframe;
                    },
                    enumerable: true,
                    configurable: true
                });
                Keyframe.prototype.setKeyword = function (keyword) {
                    return this.setNode('keyword', keyword, 0);
                };
                Keyframe.prototype.getKeyword = function () {
                    return this.keyword;
                };
                Keyframe.prototype.setIdentifier = function (node) {
                    return this.setNode('identifier', node, 0);
                };
                Keyframe.prototype.getIdentifier = function () {
                    return this.identifier;
                };
                Keyframe.prototype.getName = function () {
                    return this.identifier ? this.identifier.getText() : '';
                };
                return Keyframe;
            }(BodyDeclaration));
            var KeyframeSelector = /** @class */ (function (_super) {
                __extends(KeyframeSelector, _super);
                function KeyframeSelector(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(KeyframeSelector.prototype, "type", {
                    get: function () {
                        return NodeType.KeyframeSelector;
                    },
                    enumerable: true,
                    configurable: true
                });
                return KeyframeSelector;
            }(BodyDeclaration));
            var Import = /** @class */ (function (_super) {
                __extends(Import, _super);
                function Import(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(Import.prototype, "type", {
                    get: function () {
                        return NodeType.Import;
                    },
                    enumerable: true,
                    configurable: true
                });
                Import.prototype.setMedialist = function (node) {
                    if (node) {
                        node.attachTo(this);
                        this.medialist = node;
                        return true;
                    }
                    return false;
                };
                return Import;
            }(Node));
            var Namespace = /** @class */ (function (_super) {
                __extends(Namespace, _super);
                function Namespace(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(Namespace.prototype, "type", {
                    get: function () {
                        return NodeType.Namespace;
                    },
                    enumerable: true,
                    configurable: true
                });
                return Namespace;
            }(Node));
            var Media = /** @class */ (function (_super) {
                __extends(Media, _super);
                function Media(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(Media.prototype, "type", {
                    get: function () {
                        return NodeType.Media;
                    },
                    enumerable: true,
                    configurable: true
                });
                return Media;
            }(BodyDeclaration));
            var Supports = /** @class */ (function (_super) {
                __extends(Supports, _super);
                function Supports(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(Supports.prototype, "type", {
                    get: function () {
                        return NodeType.Supports;
                    },
                    enumerable: true,
                    configurable: true
                });
                return Supports;
            }(BodyDeclaration));
            var Document = /** @class */ (function (_super) {
                __extends(Document, _super);
                function Document(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(Document.prototype, "type", {
                    get: function () {
                        return NodeType.Document;
                    },
                    enumerable: true,
                    configurable: true
                });
                return Document;
            }(BodyDeclaration));
            var Medialist = /** @class */ (function (_super) {
                __extends(Medialist, _super);
                function Medialist(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Medialist.prototype.getMediums = function () {
                    if (!this.mediums) {
                        this.mediums = new Nodelist(this);
                    }
                    return this.mediums;
                };
                return Medialist;
            }(Node));
            var MediaQuery = /** @class */ (function (_super) {
                __extends(MediaQuery, _super);
                function MediaQuery(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(MediaQuery.prototype, "type", {
                    get: function () {
                        return NodeType.MediaQuery;
                    },
                    enumerable: true,
                    configurable: true
                });
                return MediaQuery;
            }(Node));
            var SupportsCondition = /** @class */ (function (_super) {
                __extends(SupportsCondition, _super);
                function SupportsCondition(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(SupportsCondition.prototype, "type", {
                    get: function () {
                        return NodeType.SupportsCondition;
                    },
                    enumerable: true,
                    configurable: true
                });
                return SupportsCondition;
            }(Node));
            var Page = /** @class */ (function (_super) {
                __extends(Page, _super);
                function Page(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(Page.prototype, "type", {
                    get: function () {
                        return NodeType.Page;
                    },
                    enumerable: true,
                    configurable: true
                });
                return Page;
            }(BodyDeclaration));
            var PageBoxMarginBox = /** @class */ (function (_super) {
                __extends(PageBoxMarginBox, _super);
                function PageBoxMarginBox(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(PageBoxMarginBox.prototype, "type", {
                    get: function () {
                        return NodeType.PageBoxMarginBox;
                    },
                    enumerable: true,
                    configurable: true
                });
                return PageBoxMarginBox;
            }(BodyDeclaration));
            var Expression = /** @class */ (function (_super) {
                __extends(Expression, _super);
                function Expression(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(Expression.prototype, "type", {
                    get: function () {
                        return NodeType.Expression;
                    },
                    enumerable: true,
                    configurable: true
                });
                return Expression;
            }(Node));
            var BinaryExpression = /** @class */ (function (_super) {
                __extends(BinaryExpression, _super);
                function BinaryExpression(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(BinaryExpression.prototype, "type", {
                    get: function () {
                        return NodeType.BinaryExpression;
                    },
                    enumerable: true,
                    configurable: true
                });
                BinaryExpression.prototype.setLeft = function (left) {
                    return this.setNode('left', left);
                };
                BinaryExpression.prototype.getLeft = function () {
                    return this.left;
                };
                BinaryExpression.prototype.setRight = function (right) {
                    return this.setNode('right', right);
                };
                BinaryExpression.prototype.getRight = function () {
                    return this.right;
                };
                BinaryExpression.prototype.setOperator = function (value) {
                    return this.setNode('operator', value);
                };
                BinaryExpression.prototype.getOperator = function () {
                    return this.operator;
                };
                return BinaryExpression;
            }(Node));
            var Term = /** @class */ (function (_super) {
                __extends(Term, _super);
                function Term(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(Term.prototype, "type", {
                    get: function () {
                        return NodeType.Term;
                    },
                    enumerable: true,
                    configurable: true
                });
                Term.prototype.setOperator = function (value) {
                    return this.setNode('operator', value);
                };
                Term.prototype.getOperator = function () {
                    return this.operator;
                };
                Term.prototype.setExpression = function (value) {
                    return this.setNode('expression', value);
                };
                Term.prototype.getExpression = function () {
                    return this.expression;
                };
                return Term;
            }(Node));

            var AttributeSelector = /** @class */ (function (_super) {
                __extends(AttributeSelector, _super);
                function AttributeSelector(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(AttributeSelector.prototype, "type", {
                    get: function () {
                        return NodeType.AttributeSelector;
                    },
                    enumerable: true,
                    configurable: true
                });
                AttributeSelector.prototype.setNamespacePrefix = function (value) {
                    return this.setNode('namespacePrefix', value);
                };
                AttributeSelector.prototype.getNamespacePrefix = function () {
                    return this.namespacePrefix;
                };
                AttributeSelector.prototype.setIdentifier = function (value) {
                    return this.setNode('identifier', value);
                };
                AttributeSelector.prototype.getIdentifier = function () {
                    return this.identifier;
                };
                AttributeSelector.prototype.setOperator = function (operator) {
                    return this.setNode('operator', operator);
                };
                AttributeSelector.prototype.getOperator = function () {
                    return this.operator;
                };
                AttributeSelector.prototype.setValue = function (value) {
                    return this.setNode('value', value);
                };
                AttributeSelector.prototype.getValue = function () {
                    return this.value;
                };
                return AttributeSelector;
            }(Node));

            var Operator = /** @class */ (function (_super) {
                __extends(Operator, _super);
                function Operator(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(Operator.prototype, "type", {
                    get: function () {
                        return NodeType.Operator;
                    },
                    enumerable: true,
                    configurable: true
                });
                return Operator;
            }(Node));

            var HexColorValue = /** @class */ (function (_super) {
                __extends(HexColorValue, _super);
                function HexColorValue(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(HexColorValue.prototype, "type", {
                    get: function () {
                        return NodeType.HexColorValue;
                    },
                    enumerable: true,
                    configurable: true
                });
                return HexColorValue;
            }(Node));

            var _dot = '.'.charCodeAt(0), _0 = '0'.charCodeAt(0), _9 = '9'.charCodeAt(0);
            var NumericValue = /** @class */ (function (_super) {
                __extends(NumericValue, _super);
                function NumericValue(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(NumericValue.prototype, "type", {
                    get: function () {
                        return NodeType.NumericValue;
                    },
                    enumerable: true,
                    configurable: true
                });
                NumericValue.prototype.getValue = function () {
                    var raw = this.getText();
                    var unitIdx = 0;
                    var code;
                    for (var i = 0, len = raw.length; i < len; i++) {
                        code = raw.charCodeAt(i);
                        if (!(_0 <= code && code <= _9 || code === _dot)) {
                            break;
                        }
                        unitIdx += 1;
                    }
                    return {
                        value: raw.substring(0, unitIdx),
                        unit: unitIdx < raw.length ? raw.substring(unitIdx) : undefined
                    };
                };
                return NumericValue;
            }(Node));

            var VariableDeclaration = /** @class */ (function (_super) {
                __extends(VariableDeclaration, _super);
                function VariableDeclaration(offset, length) {
                    var _this = _super.call(this, offset, length) || this;
                    _this.needsSemicolon = true;
                    return _this;
                }
                Object.defineProperty(VariableDeclaration.prototype, "type", {
                    get: function () {
                        return NodeType.VariableDeclaration;
                    },
                    enumerable: true,
                    configurable: true
                });
                VariableDeclaration.prototype.setVariable = function (node) {
                    if (node) {
                        node.attachTo(this);
                        this.variable = node;
                        return true;
                    }
                    return false;
                };
                VariableDeclaration.prototype.getVariable = function () {
                    return this.variable;
                };
                VariableDeclaration.prototype.getName = function () {
                    return this.variable ? this.variable.getName() : '';
                };
                VariableDeclaration.prototype.setValue = function (node) {
                    if (node) {
                        node.attachTo(this);
                        this.value = node;
                        return true;
                    }
                    return false;
                };
                VariableDeclaration.prototype.getValue = function () {
                    return this.value;
                };
                return VariableDeclaration;
            }(AbstractDeclaration));

            var Interpolation = /** @class */ (function (_super) {
                __extends(Interpolation, _super);
                function Interpolation(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(Interpolation.prototype, "type", {
                    get: function () {
                        return NodeType.Interpolation;
                    },
                    enumerable: true,
                    configurable: true
                });
                return Interpolation;
            }(Node));

            var Variable = /** @class */ (function (_super) {
                __extends(Variable, _super);
                function Variable(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(Variable.prototype, "type", {
                    get: function () {
                        return NodeType.VariableName;
                    },
                    enumerable: true,
                    configurable: true
                });
                Variable.prototype.getName = function () {
                    return this.getText();
                };
                return Variable;
            }(Node));

            var ExtendsReference = /** @class */ (function (_super) {
                __extends(ExtendsReference, _super);
                function ExtendsReference(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(ExtendsReference.prototype, "type", {
                    get: function () {
                        return NodeType.ExtendsReference;
                    },
                    enumerable: true,
                    configurable: true
                });
                ExtendsReference.prototype.getSelectors = function () {
                    if (!this.selectors) {
                        this.selectors = new Nodelist(this);
                    }
                    return this.selectors;
                };
                return ExtendsReference;
            }(Node));

            var MixinReference = /** @class */ (function (_super) {
                __extends(MixinReference, _super);
                function MixinReference(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(MixinReference.prototype, "type", {
                    get: function () {
                        return NodeType.MixinReference;
                    },
                    enumerable: true,
                    configurable: true
                });
                MixinReference.prototype.getNamespaces = function () {
                    if (!this.namespaces) {
                        this.namespaces = new Nodelist(this);
                    }
                    return this.namespaces;
                };
                MixinReference.prototype.setIdentifier = function (node) {
                    return this.setNode('identifier', node, 0);
                };
                MixinReference.prototype.getIdentifier = function () {
                    return this.identifier;
                };
                MixinReference.prototype.getName = function () {
                    return this.identifier ? this.identifier.getText() : '';
                };
                MixinReference.prototype.getArguments = function () {
                    if (!this.arguments) {
                        this.arguments = new Nodelist(this);
                    }
                    return this.arguments;
                };
                MixinReference.prototype.setContent = function (node) {
                    return this.setNode('content', node);
                };
                MixinReference.prototype.getContent = function () {
                    return this.content;
                };
                return MixinReference;
            }(Node));

            var MixinDeclaration = /** @class */ (function (_super) {
                __extends(MixinDeclaration, _super);
                function MixinDeclaration(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(MixinDeclaration.prototype, "type", {
                    get: function () {
                        return NodeType.MixinDeclaration;
                    },
                    enumerable: true,
                    configurable: true
                });
                MixinDeclaration.prototype.setIdentifier = function (node) {
                    return this.setNode('identifier', node, 0);
                };
                MixinDeclaration.prototype.getIdentifier = function () {
                    return this.identifier;
                };
                MixinDeclaration.prototype.getName = function () {
                    return this.identifier ? this.identifier.getText() : '';
                };
                MixinDeclaration.prototype.getParameters = function () {
                    if (!this.parameters) {
                        this.parameters = new Nodelist(this);
                    }
                    return this.parameters;
                };
                MixinDeclaration.prototype.setGuard = function (node) {
                    if (node) {
                        node.attachTo(this);
                        this.guard = node;
                    }
                    return false;
                };
                return MixinDeclaration;
            }(BodyDeclaration));

            var UnknownAtRule = /** @class */ (function (_super) {
                __extends(UnknownAtRule, _super);
                function UnknownAtRule(offset, length) {
                    return _super.call(this, offset, length) || this;
                }
                Object.defineProperty(UnknownAtRule.prototype, "type", {
                    get: function () {
                        return NodeType.UnknownAtRule;
                    },
                    enumerable: true,
                    configurable: true
                });
                UnknownAtRule.prototype.setAtRuleName = function (atRuleName) {
                    this.atRuleName = atRuleName;
                };
                UnknownAtRule.prototype.getAtRuleName = function (atRuleName) {
                    return this.atRuleName;
                };
                return UnknownAtRule;
            }(BodyDeclaration));

            var ListEntry = /** @class */ (function (_super) {
                __extends(ListEntry, _super);
                function ListEntry() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(ListEntry.prototype, "type", {
                    get: function () {
                        return NodeType.ListEntry;
                    },
                    enumerable: true,
                    configurable: true
                });
                ListEntry.prototype.setKey = function (node) {
                    return this.setNode('key', node, 0);
                };
                ListEntry.prototype.setValue = function (node) {
                    return this.setNode('value', node, 1);
                };
                return ListEntry;
            }(Node));

            var LessGuard = /** @class */ (function (_super) {
                __extends(LessGuard, _super);
                function LessGuard() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                LessGuard.prototype.getConditions = function () {
                    if (!this.conditions) {
                        this.conditions = new Nodelist(this);
                    }
                    return this.conditions;
                };
                return LessGuard;
            }(Node));

            var GuardCondition = /** @class */ (function (_super) {
                __extends(GuardCondition, _super);
                function GuardCondition() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                GuardCondition.prototype.setVariable = function (node) {
                    return this.setNode('variable', node);
                };
                return GuardCondition;
            }(Node));

    /* export */ var Level;
            (function (Level) {
                Level[Level["Ignore"] = 1] = "Ignore";
                Level[Level["Warning"] = 2] = "Warning";
                Level[Level["Error"] = 4] = "Error";
            })(Level || (Level = {}));
            var Marker = /** @class */ (function () {
                function Marker(node, rule, level, message, offset, length) {
                    if (offset === void 0) { offset = node.offset; }
                    if (length === void 0) { length = node.length; }
                    this.node = node;
                    this.rule = rule;
                    this.level = level;
                    this.message = message || rule.message;
                    this.offset = offset;
                    this.length = length;
                }
                Marker.prototype.getRule = function () {
                    return this.rule;
                };
                Marker.prototype.getLevel = function () {
                    return this.level;
                };
                Marker.prototype.getOffset = function () {
                    return this.offset;
                };
                Marker.prototype.getLength = function () {
                    return this.length;
                };
                Marker.prototype.getNode = function () {
                    return this.node;
                };
                Marker.prototype.getMessage = function () {
                    return this.message;
                };
                return Marker;
            }());

            var ParseErrorCollector = /** @class */ (function () {
                function ParseErrorCollector() {
                    this.entries = [];
                }
                ParseErrorCollector.entries = function (node) {
                    var visitor = new ParseErrorCollector();
                    node.acceptVisitor(visitor);
                    return visitor.entries;
                };
                ParseErrorCollector.prototype.visitNode = function (node) {
                    if (node.isErroneous()) {
                        node.collectIssues(this.entries);
                    }
                    return true;
                };
                return ParseErrorCollector;
            }());

            /**
             * Takes a sorted array and a function p. The array is sorted in such a way that all elements where p(x) is false
             * are located before all elements where p(x) is true.
             * @returns the least x for which p(x) is true or array.length if no element fullfills the given function.
             */
            function findFirst(array, p) {
                var low = 0, high = array.length;
                if (high === 0) {
                    return 0;
                }
                while (low < high) {
                    var mid = Math.floor((low + high) / 2);
                    if (p(array[mid])) {
                        high = mid;
                    }
                    else {
                        low = mid + 1;
                    }
                }
                return low;
            }
            var Scope = /** @class */ (function () {
                function Scope(offset, length) {
                    this.offset = offset;
                    this.length = length;
                    this.symbols = [];
                    this.parent = null;
                    this.children = [];
                }
                Scope.prototype.addChild = function (scope) {
                    this.children.push(scope);
                    scope.setParent(this);
                };
                Scope.prototype.setParent = function (scope) {
                    this.parent = scope;
                };
                Scope.prototype.findScope = function (offset, length) {
                    if (length === void 0) { length = 0; }
                    if (this.offset <= offset && this.offset + this.length > offset + length || this.offset === offset && this.length === length) {
                        return this.findInScope(offset, length);
                    }
                    return null;
                };
                Scope.prototype.findInScope = function (offset, length) {
                    if (length === void 0) { length = 0; }
                    var end = offset + length;
                    var idx = findFirst(this.children, function (s) { return s.offset > end; });
                    if (idx === 0) {
                        return this;
                    }
                    var res = this.children[idx - 1];
                    if (res.offset <= offset && res.offset + res.length >= offset + length) {
                        return res.findInScope(offset, length);
                    }
                    return this;
                };
                Scope.prototype.addSymbol = function (symbol) {
                    this.symbols.push(symbol);
                };
                Scope.prototype.getSymbol = function (name, type) {
                    for (var index = 0; index < this.symbols.length; index++) {
                        var symbol = this.symbols[index];
                        if (symbol.name === name && symbol.type === type) {
                            return symbol;
                        }
                    }
                    return null;
                };
                Scope.prototype.getSymbols = function () {
                    return this.symbols;
                };
                return Scope;
            }());

            var GlobalScope = /** @class */ (function (_super) {
                __extends(GlobalScope, _super);
                function GlobalScope() {
                    return _super.call(this, 0, Number.MAX_VALUE) || this;
                }
                return GlobalScope;
            }(Scope));

            var Symbol = /** @class */ (function () {
                function Symbol(name, value, node, type) {
                    this.name = name;
                    this.value = value;
                    this.node = node;
                    this.type = type;
                }
                return Symbol;
            }());

            var ScopeBuilder = /** @class */ (function () {
                function ScopeBuilder(scope) {
                    this.scope = scope;
                }
                ScopeBuilder.prototype.addSymbol = function (node, name, value, type) {
                    if (node.offset !== -1) {
                        var current = this.scope.findScope(node.offset, node.length);
                        if (current) {
                            current.addSymbol(new Symbol(name, value, node, type));
                        }
                    }
                };
                ScopeBuilder.prototype.addScope = function (node) {
                    if (node.offset !== -1) {
                        var current = this.scope.findScope(node.offset, node.length);
                        if (current && (current.offset !== node.offset || current.length !== node.length)) {
                            var newScope = new Scope(node.offset, node.length);
                            current.addChild(newScope);
                            return newScope;
                        }
                        return current;
                    }
                    return null;
                };
                ScopeBuilder.prototype.addSymbolToChildScope = function (scopeNode, node, name, value, type) {
                    if (scopeNode && scopeNode.offset !== -1) {
                        var current = this.addScope(scopeNode);
                        if (current) {
                            current.addSymbol(new Symbol(name, value, node, type));
                        }
                    }
                };
                ScopeBuilder.prototype.visitNode = function (node) {
                    switch (node.type) {
                        case /* nodes. */NodeType.Keyframe:
                            this.addSymbol(node, node.getName(), void 0, /* nodes. */ReferenceType.Keyframe);
                            return true;
                        case /* nodes. */NodeType.CustomPropertyDeclaration:
                            return this.visitCustomPropertyDeclarationNode(node);
                        case /* nodes. */NodeType.VariableDeclaration:
                            return this.visitVariableDeclarationNode(node);
                        case /* nodes. */NodeType.Ruleset:
                            return this.visitRuleSet(node);
                        case /* nodes. */NodeType.MixinDeclaration:
                            this.addSymbol(node, node.getName(), void 0, /* nodes. */ReferenceType.Mixin);
                            return true;
                        case /* nodes. */NodeType.FunctionDeclaration:
                            this.addSymbol(node, node.getName(), void 0, /* nodes. */ReferenceType.Function);
                            return true;
                        case /* nodes. */NodeType.FunctionParameter: {
                            return this.visitFunctionParameterNode(node);
                        }
                        case /* nodes. */NodeType.Declarations:
                            this.addScope(node);
                            return true;
                        case /* nodes. */NodeType.For:
                            var forNode = node;
                            var scopeNode = forNode.getDeclarations();
                            if (scopeNode) {
                                this.addSymbolToChildScope(scopeNode, forNode.variable, forNode.variable.getName(), void 0, /* nodes. */ReferenceType.Variable);
                            }
                            return true;
                        case /* nodes. */NodeType.Each: {
                            var eachNode = node;
                            var scopeNode_1 = eachNode.getDeclarations();
                            if (scopeNode_1) {
                                var variables = eachNode.getVariables().getChildren();
                                for (var _i = 0, variables_1 = variables; _i < variables_1.length; _i++) {
                                    var variable = variables_1[_i];
                                    this.addSymbolToChildScope(scopeNode_1, variable, variable.getName(), void 0, /* nodes. */ReferenceType.Variable);
                                }
                            }
                            return true;
                        }
                    }
                    return true;
                };
                ScopeBuilder.prototype.visitRuleSet = function (node) {
                    var current = this.scope.findScope(node.offset, node.length);
                    if (current) {
                        for (var _i = 0, _a = node.getSelectors().getChildren(); _i < _a.length; _i++) {
                            var child = _a[_i];
                            if (child instanceof /* nodes. */Selector) {
                                if (child.getChildren().length === 1) {
                                    current.addSymbol(new Symbol(child.getChild(0).getText(), void 0, child, /* nodes. */ReferenceType.Rule));
                                }
                            }
                        }
                    }
                    return true;
                };
                ScopeBuilder.prototype.visitVariableDeclarationNode = function (node) {
                    var value = node.getValue() ? node.getValue().getText() : void 0;
                    this.addSymbol(node, node.getName(), value, /* nodes. */ReferenceType.Variable);
                    return true;
                };
                ScopeBuilder.prototype.visitFunctionParameterNode = function (node) {
                    var scopeNode = node.getParent().getDeclarations();
                    if (scopeNode) {
                        var valueNode = node.getDefaultValue();
                        var value = valueNode ? valueNode.getText() : void 0;
                        this.addSymbolToChildScope(scopeNode, node, node.getName(), value, /* nodes. */ReferenceType.Variable);
                    }
                    return true;
                };
                ScopeBuilder.prototype.visitCustomPropertyDeclarationNode = function (node) {
                    var value = node.getValue() ? node.getValue().getText() : '';
                    this.addCSSVariable(node.getProperty(), node.getProperty().getName(), value, /* nodes. */ReferenceType.Variable);
                    return true;
                };
                ScopeBuilder.prototype.addCSSVariable = function (node, name, value, type) {
                    if (node.offset !== -1) {
                        this.scope.addSymbol(new Symbol(name, value, node, type));
                    }
                };
                return ScopeBuilder;
            }());

            var Symbols = /** @class */ (function () {
                function Symbols(node) {
                    this.global = new GlobalScope();
                    node.acceptVisitor(new ScopeBuilder(this.global));
                }
                Symbols.prototype.findSymbolsAtOffset = function (offset, referenceType) {
                    var scope = this.global.findScope(offset, 0);
                    var result = [];
                    var names = {};
                    while (scope) {
                        var symbols = scope.getSymbols();
                        for (var i = 0; i < symbols.length; i++) {
                            var symbol = symbols[i];
                            if (symbol.type === referenceType && !names[symbol.name]) {
                                result.push(symbol);
                                names[symbol.name] = true;
                            }
                        }
                        scope = scope.parent;
                    }
                    return result;
                };
                Symbols.prototype.internalFindSymbol = function (node, referenceTypes) {
                    var scopeNode = node;
                    if (node.parent instanceof /* nodes. */FunctionParameter && node.parent.getParent() instanceof /* nodes. */BodyDeclaration) {
                        scopeNode = node.parent.getParent().getDeclarations();
                    }
                    if (node.parent instanceof /* nodes. */FunctionArgument && node.parent.getParent() instanceof /* nodes. */Function) {
                        var funcId = node.parent.getParent().getIdentifier();
                        if (funcId) {
                            var functionSymbol = this.internalFindSymbol(funcId, [/* nodes. */ReferenceType.Function]);
                            if (functionSymbol) {
                                scopeNode = functionSymbol.node.getDeclarations();
                            }
                        }
                    }
                    if (!scopeNode) {
                        return null;
                    }
                    var name = node.getText();
                    var scope = this.global.findScope(scopeNode.offset, scopeNode.length);
                    while (scope) {
                        for (var index = 0; index < referenceTypes.length; index++) {
                            var type = referenceTypes[index];
                            var symbol = scope.getSymbol(name, type);
                            if (symbol) {
                                return symbol;
                            }
                        }
                        scope = scope.parent;
                    }
                    return null;
                };
                Symbols.prototype.evaluateReferenceTypes = function (node) {
                    if (node instanceof /* nodes. */Identifier) {
                        var referenceTypes = node.referenceTypes;
                        if (referenceTypes) {
                            return referenceTypes;
                        }
                        else {
                            if (node.isCustomProperty) {
                                return [/* nodes. */ReferenceType.Variable];
                            }
                            var decl = /* nodes. */getParentDeclaration(node);
                            if (decl) {
                                var propertyName = decl.getNonPrefixedPropertyName();
                                if ((propertyName === 'animation' || propertyName === 'animation-name')
                                    && decl.getValue() && decl.getValue().offset === node.offset) {
                                    return [/* nodes. */ReferenceType.Keyframe];
                                }
                            }
                        }
                    }
                    else if (node instanceof /* nodes. */Variable) {
                        return [/* nodes. */ReferenceType.Variable];
                    }
                    var selector = node.findAParent(/* nodes. */NodeType.Selector, /* nodes. */NodeType.ExtendsReference);
                    if (selector) {
                        return [/* nodes. */ReferenceType.Rule];
                    }
                    return null;
                };
                Symbols.prototype.findSymbolFromNode = function (node) {
                    if (!node) {
                        return null;
                    }
                    while (node.type === /* nodes. */NodeType.Interpolation) {
                        node = node.getParent();
                    }
                    var referenceTypes = this.evaluateReferenceTypes(node);
                    if (referenceTypes) {
                        return this.internalFindSymbol(node, referenceTypes);
                    }
                    return null;
                };
                Symbols.prototype.matchesSymbol = function (node, symbol) {
                    if (!node) {
                        return false;
                    }
                    while (node.type === /* nodes. */NodeType.Interpolation) {
                        node = node.getParent();
                    }
                    if (symbol.name.length !== node.length || symbol.name !== node.getText()) {
                        return false;
                    }
                    var referenceTypes = this.evaluateReferenceTypes(node);
                    if (!referenceTypes || referenceTypes.indexOf(symbol.type) === -1) {
                        return false;
                    }
                    var nodeSymbol = this.internalFindSymbol(node, referenceTypes);
                    return nodeSymbol === symbol;
                };
                Symbols.prototype.findSymbol = function (name, type, offset) {
                    var scope = this.global.findScope(offset);
                    while (scope) {
                        var symbol = scope.getSymbol(name, type);
                        if (symbol) {
                            return symbol;
                        }
                        scope = scope.parent;
                    }
                    return null;
                };
                return Symbols;
            }());
            var TokenType;
            (function (TokenType) {
                TokenType.Ident = 0;
                TokenType.AtKeyword = 1;
                TokenType.String = 2;
                TokenType.BadString = 3;
                TokenType.UnquotedString = 4;
                TokenType.Hash = 5;
                TokenType.Num = 6;
                TokenType.Percentage = 7;
                TokenType.Dimension = 8;
                TokenType.UnicodeRange = 9;
                TokenType.CDO = 10;
                TokenType.CDC = 11;
                TokenType.Colon = 12;
                TokenType.SemiColon = 13;
                TokenType.CurlyL = 14;
                TokenType.CurlyR = 15;
                TokenType.ParenthesisL = 16;
                TokenType.ParenthesisR = 17;
                TokenType.BracketL = 18;
                TokenType.BracketR = 19;
                TokenType.Whitespace = 20;
                TokenType.Includes = 21;
                TokenType.Dashmatch = 22;
                TokenType.SubstringOperator = 23;
                TokenType.PrefixOperator = 24;
                TokenType.SuffixOperator = 25;
                TokenType.Delim = 26;
                TokenType.EMS = 27;
                TokenType.EXS = 28;
                TokenType.Length = 29;
                TokenType.Angle = 30;
                TokenType.Time = 31;
                TokenType.Freq = 32;
                TokenType.Exclamation = 33;
                TokenType.Resolution = 34;
                TokenType.Comma = 35;
                TokenType.Charset = 36;
                TokenType.EscapedJavaScript = 37;
                TokenType.BadEscapedJavaScript = 38;
                TokenType.Comment = 39;
                TokenType.SingleLineComment = 40;
                TokenType.EOF = 41;
                TokenType.CustomToken = 42;
            })(TokenType || (TokenType = {}));
            var MultiLineStream = /** @class */ (function () {
                function MultiLineStream(source) {
                    this.source = source;
                    this.len = source.length;
                    this.position = 0;
                }
                MultiLineStream.prototype.substring = function (from, to) {
                    if (to === void 0) { to = this.position; }
                    return this.source.substring(from, to);
                };
                MultiLineStream.prototype.eos = function () {
                    return this.len <= this.position;
                };
                MultiLineStream.prototype.pos = function () {
                    return this.position;
                };
                MultiLineStream.prototype.goBackTo = function (pos) {
                    this.position = pos;
                };
                MultiLineStream.prototype.goBack = function (n) {
                    this.position -= n;
                };
                MultiLineStream.prototype.advance = function (n) {
                    this.position += n;
                };
                MultiLineStream.prototype.nextChar = function () {
                    return this.source.charCodeAt(this.position++) || 0;
                };
                MultiLineStream.prototype.peekChar = function (n) {
                    if (n === void 0) { n = 0; }
                    return this.source.charCodeAt(this.position + n) || 0;
                };
                MultiLineStream.prototype.lookbackChar = function (n) {
                    if (n === void 0) { n = 0; }
                    return this.source.charCodeAt(this.position - n) || 0;
                };
                MultiLineStream.prototype.advanceIfChar = function (ch) {
                    if (ch === this.source.charCodeAt(this.position)) {
                        this.position++;
                        return true;
                    }
                    return false;
                };
                MultiLineStream.prototype.advanceIfChars = function (ch) {
                    if (this.position + ch.length > this.source.length) {
                        return false;
                    }
                    var i = 0;
                    for (; i < ch.length; i++) {
                        if (this.source.charCodeAt(this.position + i) !== ch[i]) {
                            return false;
                        }
                    }
                    this.advance(i);
                    return true;
                };
                MultiLineStream.prototype.advanceWhileChar = function (condition) {
                    var posNow = this.position;
                    while (this.position < this.len && condition(this.source.charCodeAt(this.position))) {
                        this.position++;
                    }
                    return this.position - posNow;
                };
                return MultiLineStream;
            }());

            var _a = 'a'.charCodeAt(0);
            var _f = 'f'.charCodeAt(0);
            var _z = 'z'.charCodeAt(0);
            var _A = 'A'.charCodeAt(0);
            var _F = 'F'.charCodeAt(0);
            var _Z = 'Z'.charCodeAt(0);
            var _0 = '0'.charCodeAt(0);
            var _9 = '9'.charCodeAt(0);
            var _TLD = '~'.charCodeAt(0);
            var _HAT = '^'.charCodeAt(0);
            var _EQS = '='.charCodeAt(0);
            var _PIP = '|'.charCodeAt(0);
            var _MIN = '-'.charCodeAt(0);
            var _USC = '_'.charCodeAt(0);
            var _PRC = '%'.charCodeAt(0);
            var _MUL = '*'.charCodeAt(0);
            var _LPA = '('.charCodeAt(0);
            var _RPA = ')'.charCodeAt(0);
            var _LAN = '<'.charCodeAt(0);
            var _RAN = '>'.charCodeAt(0);
            var _ATS = '@'.charCodeAt(0);
            var _HSH = '#'.charCodeAt(0);
            var _DLR = '$'.charCodeAt(0);
            var _BSL = '\\'.charCodeAt(0);
            var _FSL = '/'.charCodeAt(0);
            var _NWL = '\n'.charCodeAt(0);
            var _CAR = '\r'.charCodeAt(0);
            var _LFD = '\f'.charCodeAt(0);
            var _DQO = '"'.charCodeAt(0);
            var _SQO = '\''.charCodeAt(0);
            var _WSP = ' '.charCodeAt(0);
            var _TAB = '\t'.charCodeAt(0);
            var _SEM = ';'.charCodeAt(0);
            var _COL = ':'.charCodeAt(0);
            var _CUL = '{'.charCodeAt(0);
            var _CUR = '}'.charCodeAt(0);
            var _BRL = '['.charCodeAt(0);
            var _BRR = ']'.charCodeAt(0);
            var _CMA = ','.charCodeAt(0);
            var _DOT = '.'.charCodeAt(0);
            var _BNG = '!'.charCodeAt(0);
            var staticTokenTable = {};
            staticTokenTable[_SEM] = TokenType.SemiColon;
            staticTokenTable[_COL] = TokenType.Colon;
            staticTokenTable[_CUL] = TokenType.CurlyL;
            staticTokenTable[_CUR] = TokenType.CurlyR;
            staticTokenTable[_BRR] = TokenType.BracketR;
            staticTokenTable[_BRL] = TokenType.BracketL;
            staticTokenTable[_LPA] = TokenType.ParenthesisL;
            staticTokenTable[_RPA] = TokenType.ParenthesisR;
            staticTokenTable[_CMA] = TokenType.Comma;
            var staticUnitTable = {};
            staticUnitTable['em'] = TokenType.EMS;
            staticUnitTable['ex'] = TokenType.EXS;
            staticUnitTable['px'] = TokenType.Length;
            staticUnitTable['cm'] = TokenType.Length;
            staticUnitTable['mm'] = TokenType.Length;
            staticUnitTable['in'] = TokenType.Length;
            staticUnitTable['pt'] = TokenType.Length;
            staticUnitTable['pc'] = TokenType.Length;
            staticUnitTable['deg'] = TokenType.Angle;
            staticUnitTable['rad'] = TokenType.Angle;
            staticUnitTable['grad'] = TokenType.Angle;
            staticUnitTable['ms'] = TokenType.Time;
            staticUnitTable['s'] = TokenType.Time;
            staticUnitTable['hz'] = TokenType.Freq;
            staticUnitTable['khz'] = TokenType.Freq;
            staticUnitTable['%'] = TokenType.Percentage;
            staticUnitTable['fr'] = TokenType.Percentage;
            staticUnitTable['dpi'] = TokenType.Resolution;
            staticUnitTable['dpcm'] = TokenType.Resolution;
            var Scanner = /** @class */ (function () {
                function Scanner(ignoreTriva) {
                    this.stream = new MultiLineStream('');
                    this.ignoreComment = (ignoreTriva !== false);
                    this.ignoreWhitespace = (ignoreTriva !== false);
                    this.inURL = false;
                }
                Scanner.prototype.setSource = function (input) {
                    this.stream = new MultiLineStream(input);
                };
                Scanner.prototype.finishToken = function (offset, type, text) {
                    return {
                        offset: offset,
                        len: this.stream.pos() - offset,
                        type: type,
                        text: text || this.stream.substring(offset)
                    };
                };
                Scanner.prototype.substring = function (offset, len) {
                    return this.stream.substring(offset, offset + len);
                };
                Scanner.prototype.pos = function () {
                    return this.stream.pos();
                };
                Scanner.prototype.goBackTo = function (pos) {
                    this.stream.goBackTo(pos);
                };
                Scanner.prototype.scanUnquotedString = function () {
                    var offset = this.stream.pos();
                    var content = [];
                    if (this._unquotedString(content)) {
                        return this.finishToken(offset, TokenType.UnquotedString, content.join(''));
                    }
                    return null;
                };
                Scanner.prototype.scan = function () {
                    var triviaToken = this.trivia();
                    if (triviaToken !== null) {
                        return triviaToken;
                    }
                    var offset = this.stream.pos();
                    if (this.stream.eos()) {
                        return this.finishToken(offset, TokenType.EOF);
                    }
                    return this.scanNext(offset);
                };
                Scanner.prototype.scanNext = function (offset) {
                    if (this.stream.advanceIfChars([_LAN, _BNG, _MIN, _MIN])) {
                        return this.finishToken(offset, TokenType.CDO);
                    }
                    if (this.stream.advanceIfChars([_MIN, _MIN, _RAN])) {
                        return this.finishToken(offset, TokenType.CDC);
                    }
                    var content = [];
                    if (this.ident(content)) {
                        return this.finishToken(offset, TokenType.Ident, content.join(''));
                    }
                    if (this.stream.advanceIfChar(_ATS)) {
                        content = ['@'];
                        if (this._name(content)) {
                            var keywordText = content.join('');
                            if (keywordText === '@charset') {
                                return this.finishToken(offset, TokenType.Charset, keywordText);
                            }
                            return this.finishToken(offset, TokenType.AtKeyword, keywordText);
                        }
                        else {
                            return this.finishToken(offset, TokenType.Delim);
                        }
                    }
                    if (this.stream.advanceIfChar(_HSH)) {
                        content = ['#'];
                        if (this._name(content)) {
                            return this.finishToken(offset, TokenType.Hash, content.join(''));
                        }
                        else {
                            return this.finishToken(offset, TokenType.Delim);
                        }
                    }
                    if (this.stream.advanceIfChar(_BNG)) {
                        return this.finishToken(offset, TokenType.Exclamation);
                    }
                    if (this._number()) {
                        var pos = this.stream.pos();
                        content = [this.stream.substring(offset, pos)];
                        if (this.stream.advanceIfChar(_PRC)) {
                            return this.finishToken(offset, TokenType.Percentage);
                        }
                        else if (this.ident(content)) {
                            var dim = this.stream.substring(pos).toLowerCase();
                            var tokenType_1 = staticUnitTable[dim];
                            if (typeof tokenType_1 !== 'undefined') {
                                return this.finishToken(offset, tokenType_1, content.join(''));
                            }
                            else {
                                return this.finishToken(offset, TokenType.Dimension, content.join(''));
                            }
                        }
                        return this.finishToken(offset, TokenType.Num);
                    }
                    content = [];
                    var tokenType = this._string(content);
                    if (tokenType !== null) {
                        return this.finishToken(offset, tokenType, content.join(''));
                    }
                    tokenType = staticTokenTable[this.stream.peekChar()];
                    if (typeof tokenType !== 'undefined') {
                        this.stream.advance(1);
                        return this.finishToken(offset, tokenType);
                    }
                    if (this.stream.peekChar(0) === _TLD && this.stream.peekChar(1) === _EQS) {
                        this.stream.advance(2);
                        return this.finishToken(offset, TokenType.Includes);
                    }
                    if (this.stream.peekChar(0) === _PIP && this.stream.peekChar(1) === _EQS) {
                        this.stream.advance(2);
                        return this.finishToken(offset, TokenType.Dashmatch);
                    }
                    if (this.stream.peekChar(0) === _MUL && this.stream.peekChar(1) === _EQS) {
                        this.stream.advance(2);
                        return this.finishToken(offset, TokenType.SubstringOperator);
                    }
                    if (this.stream.peekChar(0) === _HAT && this.stream.peekChar(1) === _EQS) {
                        this.stream.advance(2);
                        return this.finishToken(offset, TokenType.PrefixOperator);
                    }
                    if (this.stream.peekChar(0) === _DLR && this.stream.peekChar(1) === _EQS) {
                        this.stream.advance(2);
                        return this.finishToken(offset, TokenType.SuffixOperator);
                    }
                    this.stream.nextChar();
                    return this.finishToken(offset, TokenType.Delim);
                };
                Scanner.prototype._matchWordAnyCase = function (characters) {
                    var index = 0;
                    this.stream.advanceWhileChar(function (ch) {
                        var result = characters[index] === ch || characters[index + 1] === ch;
                        if (result) {
                            index += 2;
                        }
                        return result;
                    });
                    if (index === characters.length) {
                        return true;
                    }
                    else {
                        this.stream.goBack(index / 2);
                        return false;
                    }
                };
                Scanner.prototype.trivia = function () {
                    while (true) {
                        var offset = this.stream.pos();
                        if (this._whitespace()) {
                            if (!this.ignoreWhitespace) {
                                return this.finishToken(offset, TokenType.Whitespace);
                            }
                        }
                        else if (this.comment()) {
                            if (!this.ignoreComment) {
                                return this.finishToken(offset, TokenType.Comment);
                            }
                        }
                        else {
                            return null;
                        }
                    }
                };
                Scanner.prototype.comment = function () {
                    if (this.stream.advanceIfChars([_FSL, _MUL])) {
                        var success_1 = false, hot_1 = false;
                        this.stream.advanceWhileChar(function (ch) {
                            if (hot_1 && ch === _FSL) {
                                success_1 = true;
                                return false;
                            }
                            hot_1 = ch === _MUL;
                            return true;
                        });
                        if (success_1) {
                            this.stream.advance(1);
                        }
                        return true;
                    }
                    return false;
                };
                Scanner.prototype._number = function () {
                    var npeek = 0, ch;
                    if (this.stream.peekChar() === _DOT) {
                        npeek = 1;
                    }
                    ch = this.stream.peekChar(npeek);
                    if (ch >= _0 && ch <= _9) {
                        this.stream.advance(npeek + 1);
                        this.stream.advanceWhileChar(function (ch) {
                            return ch >= _0 && ch <= _9 || npeek === 0 && ch === _DOT;
                        });
                        return true;
                    }
                    return false;
                };
                Scanner.prototype._newline = function (result) {
                    var ch = this.stream.peekChar();
                    switch (ch) {
                        case _CAR:
                        case _LFD:
                        case _NWL:
                            this.stream.advance(1);
                            result.push(String.fromCharCode(ch));
                            if (ch === _CAR && this.stream.advanceIfChar(_NWL)) {
                                result.push('\n');
                            }
                            return true;
                    }
                    return false;
                };
                Scanner.prototype._escape = function (result, includeNewLines) {
                    var ch = this.stream.peekChar();
                    if (ch === _BSL) {
                        this.stream.advance(1);
                        ch = this.stream.peekChar();
                        var hexNumCount = 0;
                        while (hexNumCount < 6 && (ch >= _0 && ch <= _9 || ch >= _a && ch <= _f || ch >= _A && ch <= _F)) {
                            this.stream.advance(1);
                            ch = this.stream.peekChar();
                            hexNumCount++;
                        }
                        if (hexNumCount > 0) {
                            try {
                                var hexVal = parseInt(this.stream.substring(this.stream.pos() - hexNumCount), 16);
                                if (hexVal) {
                                    result.push(String.fromCharCode(hexVal));
                                }
                            }
                            catch (e) {
                            }
                            if (ch === _WSP || ch === _TAB) {
                                this.stream.advance(1);
                            }
                            else {
                                this._newline([]);
                            }
                            return true;
                        }
                        if (ch !== _CAR && ch !== _LFD && ch !== _NWL) {
                            this.stream.advance(1);
                            result.push(String.fromCharCode(ch));
                            return true;
                        }
                        else if (includeNewLines) {
                            return this._newline(result);
                        }
                    }
                    return false;
                };
                Scanner.prototype._stringChar = function (closeQuote, result) {
                    var ch = this.stream.peekChar();
                    if (ch !== 0 && ch !== closeQuote && ch !== _BSL && ch !== _CAR && ch !== _LFD && ch !== _NWL) {
                        this.stream.advance(1);
                        result.push(String.fromCharCode(ch));
                        return true;
                    }
                    return false;
                };
                Scanner.prototype._string = function (result) {
                    if (this.stream.peekChar() === _SQO || this.stream.peekChar() === _DQO) {
                        var closeQuote = this.stream.nextChar();
                        result.push(String.fromCharCode(closeQuote));
                        while (this._stringChar(closeQuote, result) || this._escape(result, true)) {
                        }
                        if (this.stream.peekChar() === closeQuote) {
                            this.stream.nextChar();
                            result.push(String.fromCharCode(closeQuote));
                            return TokenType.String;
                        }
                        else {
                            return TokenType.BadString;
                        }
                    }
                    return null;
                };
                Scanner.prototype._unquotedChar = function (result) {
                    var ch = this.stream.peekChar();
                    if (ch !== 0 && ch !== _BSL && ch !== _SQO && ch !== _DQO && ch !== _LPA && ch !== _RPA && ch !== _WSP && ch !== _TAB && ch !== _NWL && ch !== _LFD && ch !== _CAR) {
                        this.stream.advance(1);
                        result.push(String.fromCharCode(ch));
                        return true;
                    }
                    return false;
                };
                Scanner.prototype._unquotedString = function (result) {
                    var hasContent = false;
                    while (this._unquotedChar(result) || this._escape(result)) {
                        hasContent = true;
                    }
                    return hasContent;
                };
                Scanner.prototype._whitespace = function () {
                    var n = this.stream.advanceWhileChar(function (ch) {
                        return ch === _WSP || ch === _TAB || ch === _NWL || ch === _LFD || ch === _CAR;
                    });
                    return n > 0;
                };
                Scanner.prototype._name = function (result) {
                    var matched = false;
                    while (this._identChar(result) || this._escape(result)) {
                        matched = true;
                    }
                    return matched;
                };
                Scanner.prototype.ident = function (result) {
                    var pos = this.stream.pos();
                    var hasMinus = this._minus(result);
                    if (hasMinus && this._minus(result) /* -- */) {
                        if (this._identFirstChar(result) || this._escape(result)) {
                            while (this._identChar(result) || this._escape(result)) {
                            }
                            return true;
                        }
                    }
                    else if (this._identFirstChar(result) || this._escape(result)) {
                        while (this._identChar(result) || this._escape(result)) {
                        }
                        return true;
                    }
                    this.stream.goBackTo(pos);
                    return false;
                };
                Scanner.prototype._identFirstChar = function (result) {
                    var ch = this.stream.peekChar();
                    if (ch === _USC ||
                        ch >= _a && ch <= _z ||
                        ch >= _A && ch <= _Z ||
                        ch >= 0x80 && ch <= 0xFFFF) {
                        this.stream.advance(1);
                        result.push(String.fromCharCode(ch));
                        return true;
                    }
                    return false;
                };
                Scanner.prototype._minus = function (result) {
                    var ch = this.stream.peekChar();
                    if (ch === _MIN) {
                        this.stream.advance(1);
                        result.push(String.fromCharCode(ch));
                        return true;
                    }
                    return false;
                };
                Scanner.prototype._identChar = function (result) {
                    var ch = this.stream.peekChar();
                    if (ch === _USC ||
                        ch === _MIN ||
                        ch >= _a && ch <= _z ||
                        ch >= _A && ch <= _Z ||
                        ch >= _0 && ch <= _9 ||
                        ch >= 0x80 && ch <= 0xFFFF) {
                        this.stream.advance(1);
                        result.push(String.fromCharCode(ch));
                        return true;
                    }
                    return false;
                };
                return Scanner;
            }());
            var Parser = /** @class */ (function () {
                function Parser(scnr, ignoreTriva) {
                    if (scnr === void 0) { scnr = new Scanner(ignoreTriva); }
                    this.keyframeRegex = /^@(\-(webkit|ms|moz|o)\-)?keyframes$/i;
                    this.scanner = scnr;
                    this.token = null;
                    this.prevToken = null;
                }
                Parser.prototype.peekIdent = function (text) {
                    return TokenType.Ident === this.token.type && text.length === this.token.text.length && text === this.token.text.toLowerCase();
                };
                Parser.prototype.peekKeyword = function (text) {
                    return TokenType.AtKeyword === this.token.type && text.length === this.token.text.length && text === this.token.text.toLowerCase();
                };
                Parser.prototype.peekDelim = function (text) {
                    return TokenType.Delim === this.token.type && text === this.token.text;
                };
                Parser.prototype.peek = function (type) {
                    return type === this.token.type;
                };
                Parser.prototype.peekRegExp = function (type, regEx) {
                    if (type !== this.token.type) {
                        return false;
                    }
                    return regEx.test(this.token.text);
                };
                Parser.prototype.hasWhitespace = function () {
                    return this.prevToken && (this.prevToken.offset + this.prevToken.len !== this.token.offset);
                };
                Parser.prototype.consumeToken = function () {
                    this.prevToken = this.token;
                    this.token = this.scanner.scan();
                };
                Parser.prototype.mark = function () {
                    return {
                        prev: this.prevToken,
                        curr: this.token,
                        pos: this.scanner.pos()
                    };
                };
                Parser.prototype.restoreAtMark = function (mark) {
                    this.prevToken = mark.prev;
                    this.token = mark.curr;
                    this.scanner.goBackTo(mark.pos);
                };
                Parser.prototype.try = function (func) {
                    var pos = this.mark();
                    var node = func();
                    if (!node) {
                        this.restoreAtMark(pos);
                        return null;
                    }
                    return node;
                };
                Parser.prototype.acceptOneKeyword = function (keywords) {
                    if (TokenType.AtKeyword === this.token.type) {
                        for (var _i = 0, keywords_1 = keywords; _i < keywords_1.length; _i++) {
                            var keyword = keywords_1[_i];
                            if (keyword.length === this.token.text.length && keyword === this.token.text.toLowerCase()) {
                                this.consumeToken();
                                return true;
                            }
                        }
                    }
                    return false;
                };
                Parser.prototype.accept = function (type) {
                    if (type === this.token.type) {
                        this.consumeToken();
                        return true;
                    }
                    return false;
                };
                Parser.prototype.acceptIdent = function (text) {
                    if (this.peekIdent(text)) {
                        this.consumeToken();
                        return true;
                    }
                    return false;
                };
                Parser.prototype.acceptKeyword = function (text) {
                    if (this.peekKeyword(text)) {
                        this.consumeToken();
                        return true;
                    }
                    return false;
                };
                Parser.prototype.acceptDelim = function (text) {
                    if (this.peekDelim(text)) {
                        this.consumeToken();
                        return true;
                    }
                    return false;
                };
                Parser.prototype.acceptUnquotedString = function () {
                    var pos = this.scanner.pos();
                    this.scanner.goBackTo(this.token.offset);
                    var unquoted = this.scanner.scanUnquotedString();
                    if (unquoted) {
                        this.token = unquoted;
                        this.consumeToken();
                        return true;
                    }
                    this.scanner.goBackTo(pos);
                    return false;
                };
                Parser.prototype.resync = function (resyncTokens, resyncStopTokens) {
                    while (true) {
                        if (resyncTokens && resyncTokens.indexOf(this.token.type) !== -1) {
                            this.consumeToken();
                            return true;
                        }
                        else if (resyncStopTokens && resyncStopTokens.indexOf(this.token.type) !== -1) {
                            return true;
                        }
                        else {
                            if (this.token.type === TokenType.EOF) {
                                return false;
                            }
                            this.token = this.scanner.scan();
                        }
                    }
                };
                Parser.prototype.createNode = function (nodeType) {
                    return new /* nodes. */Node(this.token.offset, this.token.len, nodeType);
                };
                Parser.prototype.create = function (ctor) {
                    return new ctor(this.token.offset, this.token.len);
                };
                Parser.prototype.finish = function (node, error, resyncTokens, resyncStopTokens) {
                    if (!(node instanceof /* nodes. */Nodelist)) {
                        if (error) {
                            this.markError(node, error, resyncTokens, resyncStopTokens);
                        }
                        if (this.prevToken !== null) {
                            var prevEnd = this.prevToken.offset + this.prevToken.len;
                            node.length = prevEnd > node.offset ? prevEnd - node.offset : 0;
                        }
                    }
                    return node;
                };
                Parser.prototype.markError = function (node, error, resyncTokens, resyncStopTokens) {
                    if (this.token !== this.lastErrorToken) {
                        node.addIssue(new /* nodes. */Marker(node, error, /* nodes. */Level.Error, null, this.token.offset, this.token.len));
                        this.lastErrorToken = this.token;
                    }
                    if (resyncTokens || resyncStopTokens) {
                        this.resync(resyncTokens, resyncStopTokens);
                    }
                };
                Parser.prototype.parseStylesheet = function (text) {
                    var textProvider = function (offset, length) {
                        return text.substr(offset, length);
                    };
                    return this.internalParse(text, this._parseStylesheet, textProvider);
                };
                Parser.prototype.internalParse = function (input, parseFunc, textProvider) {
                    this.scanner.setSource(input);
                    this.token = this.scanner.scan();
                    var node = parseFunc.bind(this)();
                    if (node) {
                        if (textProvider) {
                            node.textProvider = textProvider;
                        }
                        else {
                            node.textProvider = function (offset, length) { return input.substr(offset, length); };
                        }
                    }
                    return node;
                };
                Parser.prototype._parseStylesheet = function () {
                    var node = this.create(/* nodes. */Stylesheet);
                    node.addChild(this._parseCharset());
                    var inRecovery = false;
                    do {
                        var hasMatch = false;
                        do {
                            hasMatch = false;
                            var statement = this._parseStylesheetStatement();
                            if (statement) {
                                node.addChild(statement);
                                hasMatch = true;
                                inRecovery = false;
                                if (!this.peek(TokenType.EOF) && this._needsSemicolonAfter(statement) && !this.accept(TokenType.SemiColon)) {
                                    this.markError(node, ParseError.SemiColonExpected);
                                }
                            }
                            while (this.accept(TokenType.SemiColon) || this.accept(TokenType.CDO) || this.accept(TokenType.CDC)) {
                                hasMatch = true;
                                inRecovery = false;
                            }
                        } while (hasMatch);
                        if (this.peek(TokenType.EOF)) {
                            break;
                        }
                        if (!inRecovery) {
                            if (this.peek(TokenType.AtKeyword)) {
                                this.markError(node, ParseError.UnknownAtRule);
                            }
                            else {
                                this.markError(node, ParseError.RuleOrSelectorExpected);
                            }
                            inRecovery = true;
                        }
                        this.consumeToken();
                    } while (!this.peek(TokenType.EOF));
                    return this.finish(node);
                };
                Parser.prototype._parseStylesheetStatement = function (isNested) {
                    if (isNested === void 0) { isNested = false; }
                    if (this.peek(TokenType.AtKeyword)) {
                        return this._parseStylesheetAtStatement(isNested);
                    }
                    return this._parseRuleset(isNested);
                };
                Parser.prototype._parseStylesheetAtStatement = function (isNested) {
                    if (isNested === void 0) { isNested = false; }
                    return this._parseImport()
                        || this._parseMedia(isNested)
                        || this._parsePage()
                        || this._parseFontFace()
                        || this._parseKeyframe()
                        || this._parseSupports(isNested)
                        || this._parseViewPort()
                        || this._parseNamespace()
                        || this._parseDocument()
                        || this._parseUnknownAtRule();
                };
                Parser.prototype._tryParseRuleset = function (isNested) {
                    var mark = this.mark();
                    if (this._parseSelector(isNested)) {
                        while (this.accept(TokenType.Comma) && this._parseSelector(isNested)) {
                        }
                        if (this.accept(TokenType.CurlyL)) {
                            this.restoreAtMark(mark);
                            return this._parseRuleset(isNested);
                        }
                    }
                    this.restoreAtMark(mark);
                    return null;
                };
                Parser.prototype._parseRuleset = function (isNested) {
                    if (isNested === void 0) { isNested = false; }
                    var node = this.create(/* nodes. */RuleSet);
                    var selectors = node.getSelectors();
                    if (!selectors.addChild(this._parseSelector(isNested))) {
                        return null;
                    }
                    while (this.accept(TokenType.Comma)) {
                        if (!selectors.addChild(this._parseSelector(isNested))) {
                            return this.finish(node, ParseError.SelectorExpected);
                        }
                    }
                    return this._parseBody(node, this._parseRuleSetDeclaration.bind(this));
                };
                Parser.prototype._parseRuleSetDeclaration = function () {
                    return this._parseAtApply()
                        || this._tryParseCustomPropertyDeclaration()
                        || this._parseDeclaration()
                        || this._parseUnknownAtRule();
                };
                /**
                 * Parses declarations like:
                 *   @apply --my-theme;
                 *
                 * Follows https://tabatkins.github.io/specs/css-apply-rule/#using
                 */
                Parser.prototype._parseAtApply = function () {
                    if (!this.peekKeyword('@apply')) {
                        return null;
                    }
                    var node = this.create(/* nodes. */AtApplyRule);
                    this.consumeToken();
                    if (!node.setIdentifier(this._parseIdent([/* nodes. */ReferenceType.Variable]))) {
                        return this.finish(node, ParseError.IdentifierExpected);
                    }
                    return this.finish(node);
                };
                Parser.prototype._needsSemicolonAfter = function (node) {
                    switch (node.type) {
                        case /* nodes. */NodeType.Keyframe:
                        case /* nodes. */NodeType.ViewPort:
                        case /* nodes. */NodeType.Media:
                        case /* nodes. */NodeType.Ruleset:
                        case /* nodes. */NodeType.Namespace:
                        case /* nodes. */NodeType.If:
                        case /* nodes. */NodeType.For:
                        case /* nodes. */NodeType.Each:
                        case /* nodes. */NodeType.While:
                        case /* nodes. */NodeType.MixinDeclaration:
                        case /* nodes. */NodeType.FunctionDeclaration:
                            return false;
                        case /* nodes. */NodeType.ExtendsReference:
                        case /* nodes. */NodeType.MixinContent:
                        case /* nodes. */NodeType.ReturnStatement:
                        case /* nodes. */NodeType.MediaQuery:
                        case /* nodes. */NodeType.Debug:
                        case /* nodes. */NodeType.Import:
                        case /* nodes. */NodeType.AtApplyRule:
                        case /* nodes. */NodeType.CustomPropertyDeclaration:
                            return true;
                        case /* nodes. */NodeType.VariableDeclaration:
                            return node.needsSemicolon;
                        case /* nodes. */NodeType.MixinReference:
                            return !node.getContent();
                        case /* nodes. */NodeType.Declaration:
                            return !node.getNestedProperties();
                    }
                    return false;
                };
                Parser.prototype._parseDeclarations = function (parseDeclaration) {
                    var node = this.create(/* nodes. */Declarations);
                    if (!this.accept(TokenType.CurlyL)) {
                        return null;
                    }
                    var decl = parseDeclaration();
                    while (node.addChild(decl)) {
                        if (this.peek(TokenType.CurlyR)) {
                            break;
                        }
                        if (this._needsSemicolonAfter(decl) && !this.accept(TokenType.SemiColon)) {
                            return this.finish(node, ParseError.SemiColonExpected, [TokenType.SemiColon, TokenType.CurlyR]);
                        }
                        while (this.accept(TokenType.SemiColon)) {
                        }
                        decl = parseDeclaration();
                    }
                    if (!this.accept(TokenType.CurlyR)) {
                        return this.finish(node, ParseError.RightCurlyExpected, [TokenType.CurlyR, TokenType.SemiColon]);
                    }
                    return this.finish(node);
                };
                Parser.prototype._parseBody = function (node, parseDeclaration) {
                    if (!node.setDeclarations(this._parseDeclarations(parseDeclaration))) {
                        return this.finish(node, ParseError.LeftCurlyExpected, [TokenType.CurlyR, TokenType.SemiColon]);
                    }
                    return this.finish(node);
                };
                Parser.prototype._parseSelector = function (isNested) {
                    var node = this.create(/* nodes. */Selector);
                    var hasContent = false;
                    if (isNested) {
                        hasContent = node.addChild(this._parseCombinator());
                    }
                    while (node.addChild(this._parseSimpleSelector())) {
                        hasContent = true;
                        node.addChild(this._parseCombinator());
                    }
                    return hasContent ? this.finish(node) : null;
                };
                Parser.prototype._parseDeclaration = function (resyncStopTokens) {
                    var node = this.create(/* nodes. */Declaration);
                    if (!node.setProperty(this._parseProperty())) {
                        return null;
                    }
                    if (!this.accept(TokenType.Colon)) {
                        return this.finish(node, ParseError.ColonExpected, [TokenType.Colon], resyncStopTokens);
                    }
                    node.colonPosition = this.prevToken.offset;
                    if (!node.setValue(this._parseExpr())) {
                        return this.finish(node, ParseError.PropertyValueExpected);
                    }
                    node.addChild(this._parsePrio());
                    if (this.peek(TokenType.SemiColon)) {
                        node.semicolonPosition = this.token.offset;
                    }
                    return this.finish(node);
                };
                Parser.prototype._tryParseCustomPropertyDeclaration = function () {
                    if (!this.peekRegExp(TokenType.Ident, /^--/)) {
                        return null;
                    }
                    var node = this.create(/* nodes. */CustomPropertyDeclaration);
                    if (!node.setProperty(this._parseProperty())) {
                        return null;
                    }
                    if (!this.accept(TokenType.Colon)) {
                        return this.finish(node, ParseError.ColonExpected, [TokenType.Colon]);
                    }
                    node.colonPosition = this.prevToken.offset;
                    var mark = this.mark();
                    if (this.peek(TokenType.CurlyL)) {
                        var propertySet = this.create(/* nodes. */CustomPropertySet);
                        var declarations = this._parseDeclarations(this._parseRuleSetDeclaration.bind(this));
                        if (propertySet.setDeclarations(declarations) && !declarations.isErroneous(true)) {
                            propertySet.addChild(this._parsePrio());
                            if (this.peek(TokenType.SemiColon)) {
                                this.finish(propertySet);
                                node.setPropertySet(propertySet);
                                node.semicolonPosition = this.token.offset;
                                return this.finish(node);
                            }
                        }
                        this.restoreAtMark(mark);
                    }
                    var expression = this._parseExpr();
                    if (expression && !expression.isErroneous(true)) {
                        this._parsePrio();
                        if (this.peek(TokenType.SemiColon)) {
                            node.setValue(expression);
                            node.semicolonPosition = this.token.offset;
                            return this.finish(node);
                        }
                    }
                    this.restoreAtMark(mark);
                    node.addChild(this._parseCustomPropertyValue());
                    node.addChild(this._parsePrio());
                    if (this.token.offset === node.colonPosition + 1) {
                        return this.finish(node, ParseError.PropertyValueExpected);
                    }
                    return this.finish(node);
                };
                /**
                 * Parse custom property values.
                 *
                 * Based on https://www.w3.org/TR/css-variables/#syntax
                 *
                 * This code is somewhat unusual, as the allowed syntax is incredibly broad,
                 * parsing almost any sequence of tokens, save for a small set of exceptions.
                 * Unbalanced delimitors, invalid tokens, and declaration
                 * terminators like semicolons and !important directives (when not inside
                 * of delimitors).
                 */
                Parser.prototype._parseCustomPropertyValue = function () {
                    var node = this.create(/* nodes. */Node);
                    var isTopLevel = function () { return curlyDepth === 0 && parensDepth === 0 && bracketsDepth === 0; };
                    var curlyDepth = 0;
                    var parensDepth = 0;
                    var bracketsDepth = 0;
                    done: while (true) {
                        switch (this.token.type) {
                            case TokenType.SemiColon:
                                if (isTopLevel()) {
                                    break done;
                                }
                                break;
                            case TokenType.Exclamation:
                                if (isTopLevel()) {
                                    break done;
                                }
                                break;
                            case TokenType.CurlyL:
                                curlyDepth++;
                                break;
                            case TokenType.CurlyR:
                                curlyDepth--;
                                if (curlyDepth < 0) {
                                    if (parensDepth === 0 && bracketsDepth === 0) {
                                        break done;
                                    }
                                    return this.finish(node, ParseError.LeftCurlyExpected);
                                }
                                break;
                            case TokenType.ParenthesisL:
                                parensDepth++;
                                break;
                            case TokenType.ParenthesisR:
                                parensDepth--;
                                if (parensDepth < 0) {
                                    return this.finish(node, ParseError.LeftParenthesisExpected);
                                }
                                break;
                            case TokenType.BracketL:
                                bracketsDepth++;
                                break;
                            case TokenType.BracketR:
                                bracketsDepth--;
                                if (bracketsDepth < 0) {
                                    return this.finish(node, ParseError.LeftSquareBracketExpected);
                                }
                                break;
                            case TokenType.BadString:
                                break done;
                            case TokenType.EOF:
                                var error = ParseError.RightCurlyExpected;
                                if (bracketsDepth > 0) {
                                    error = ParseError.RightSquareBracketExpected;
                                }
                                else if (parensDepth > 0) {
                                    error = ParseError.RightParenthesisExpected;
                                }
                                return this.finish(node, error);
                        }
                        this.consumeToken();
                    }
                    return this.finish(node);
                };
                Parser.prototype._tryToParseDeclaration = function () {
                    var mark = this.mark();
                    if (this._parseProperty() && this.accept(TokenType.Colon)) {
                        this.restoreAtMark(mark);
                        return this._parseDeclaration();
                    }
                    this.restoreAtMark(mark);
                    return null;
                };
                Parser.prototype._parseProperty = function () {
                    var node = this.create(/* nodes. */Property);
                    var mark = this.mark();
                    if (this.acceptDelim('*') || this.acceptDelim('_')) {
                        if (this.hasWhitespace()) {
                            this.restoreAtMark(mark);
                            return null;
                        }
                    }
                    if (node.setIdentifier(this._parsePropertyIdentifier())) {
                        return this.finish(node);
                    }
                    return null;
                };
                Parser.prototype._parsePropertyIdentifier = function () {
                    return this._parseIdent();
                };
                Parser.prototype._parseCharset = function () {
                    if (!this.peek(TokenType.Charset)) {
                        return null;
                    }
                    var node = this.create(/* nodes. */Node);
                    this.consumeToken();
                    if (!this.accept(TokenType.String)) {
                        return this.finish(node, ParseError.IdentifierExpected);
                    }
                    if (!this.accept(TokenType.SemiColon)) {
                        return this.finish(node, ParseError.SemiColonExpected);
                    }
                    return this.finish(node);
                };
                Parser.prototype._parseImport = function () {
                    if (!this.peekKeyword('@import')) {
                        return null;
                    }
                    var node = this.create(/* nodes. */Import);
                    this.consumeToken();
                    if (!node.addChild(this._parseURILiteral()) && !node.addChild(this._parseStringLiteral())) {
                        return this.finish(node, ParseError.URIOrStringExpected);
                    }
                    if (!this.peek(TokenType.SemiColon) && !this.peek(TokenType.EOF)) {
                        node.setMedialist(this._parseMediaQueryList());
                    }
                    return this.finish(node);
                };
                Parser.prototype._parseNamespace = function () {
                    if (!this.peekKeyword('@namespace')) {
                        return null;
                    }
                    var node = this.create(/* nodes. */Namespace);
                    this.consumeToken();
                    if (!node.addChild(this._parseURILiteral())) {
                        node.addChild(this._parseIdent());
                        if (!node.addChild(this._parseURILiteral()) && !node.addChild(this._parseStringLiteral())) {
                            return this.finish(node, ParseError.URIExpected, [TokenType.SemiColon]);
                        }
                    }
                    if (!this.accept(TokenType.SemiColon)) {
                        return this.finish(node, ParseError.SemiColonExpected);
                    }
                    return this.finish(node);
                };
                Parser.prototype._parseFontFace = function () {
                    if (!this.peekKeyword('@font-face')) {
                        return null;
                    }
                    var node = this.create(/* nodes. */FontFace);
                    this.consumeToken();
                    return this._parseBody(node, this._parseRuleSetDeclaration.bind(this));
                };
                Parser.prototype._parseViewPort = function () {
                    if (!this.peekKeyword('@-ms-viewport') &&
                        !this.peekKeyword('@-o-viewport') &&
                        !this.peekKeyword('@viewport')) {
                        return null;
                    }
                    var node = this.create(/* nodes. */ViewPort);
                    this.consumeToken();
                    return this._parseBody(node, this._parseRuleSetDeclaration.bind(this));
                };
                Parser.prototype._parseKeyframe = function () {
                    if (!this.peekRegExp(TokenType.AtKeyword, this.keyframeRegex)) {
                        return null;
                    }
                    var node = this.create(/* nodes. */Keyframe);
                    var atNode = this.create(/* nodes. */Node);
                    this.consumeToken();
                    node.setKeyword(this.finish(atNode));
                    if (atNode.getText() === '@-ms-keyframes') {
                        this.markError(atNode, ParseError.UnknownKeyword);
                    }
                    if (!node.setIdentifier(this._parseKeyframeIdent())) {
                        return this.finish(node, ParseError.IdentifierExpected, [TokenType.CurlyR]);
                    }
                    return this._parseBody(node, this._parseKeyframeSelector.bind(this));
                };
                Parser.prototype._parseKeyframeIdent = function () {
                    return this._parseIdent([/* nodes. */ReferenceType.Keyframe]);
                };
                Parser.prototype._parseKeyframeSelector = function () {
                    var node = this.create(/* nodes. */KeyframeSelector);
                    if (!node.addChild(this._parseIdent()) && !this.accept(TokenType.Percentage)) {
                        return null;
                    }
                    while (this.accept(TokenType.Comma)) {
                        if (!node.addChild(this._parseIdent()) && !this.accept(TokenType.Percentage)) {
                            return this.finish(node, ParseError.PercentageExpected);
                        }
                    }
                    return this._parseBody(node, this._parseRuleSetDeclaration.bind(this));
                };
                Parser.prototype._tryParseKeyframeSelector = function () {
                    var node = this.create(/* nodes. */KeyframeSelector);
                    var pos = this.mark();
                    if (!node.addChild(this._parseIdent()) && !this.accept(TokenType.Percentage)) {
                        return null;
                    }
                    while (this.accept(TokenType.Comma)) {
                        if (!node.addChild(this._parseIdent()) && !this.accept(TokenType.Percentage)) {
                            this.restoreAtMark(pos);
                            return null;
                        }
                    }
                    if (!this.peek(TokenType.CurlyL)) {
                        this.restoreAtMark(pos);
                        return null;
                    }
                    return this._parseBody(node, this._parseRuleSetDeclaration.bind(this));
                };
                Parser.prototype._parseSupports = function (isNested) {
                    if (isNested === void 0) { isNested = false; }
                    if (!this.peekKeyword('@supports')) {
                        return null;
                    }
                    var node = this.create(/* nodes. */Supports);
                    this.consumeToken();
                    node.addChild(this._parseSupportsCondition());
                    return this._parseBody(node, this._parseSupportsDeclaration.bind(this, isNested));
                };
                Parser.prototype._parseSupportsDeclaration = function (isNested) {
                    if (isNested === void 0) { isNested = false; }
                    if (isNested) {
                        return this._tryParseRuleset(true)
                            || this._tryToParseDeclaration()
                            || this._parseStylesheetStatement(true);
                    }
                    return this._parseStylesheetStatement(false);
                };
                Parser.prototype._parseSupportsCondition = function () {
                    var node = this.create(/* nodes. */SupportsCondition);
                    if (this.acceptIdent('not')) {
                        node.addChild(this._parseSupportsConditionInParens());
                    }
                    else {
                        node.addChild(this._parseSupportsConditionInParens());
                        if (this.peekRegExp(TokenType.Ident, /^(and|or)$/i)) {
                            var text = this.token.text.toLowerCase();
                            while (this.acceptIdent(text)) {
                                node.addChild(this._parseSupportsConditionInParens());
                            }
                        }
                    }
                    return this.finish(node);
                };
                Parser.prototype._parseSupportsConditionInParens = function () {
                    var node = this.create(/* nodes. */SupportsCondition);
                    if (this.accept(TokenType.ParenthesisL)) {
                        node.lParent = this.prevToken.offset;
                        if (!node.addChild(this._tryToParseDeclaration())) {
                            if (!this._parseSupportsCondition()) {
                                return this.finish(node, ParseError.ConditionExpected);
                            }
                        }
                        if (!this.accept(TokenType.ParenthesisR)) {
                            return this.finish(node, ParseError.RightParenthesisExpected, [TokenType.ParenthesisR], []);
                        }
                        node.rParent = this.prevToken.offset;
                        return this.finish(node);
                    }
                    else if (this.peek(TokenType.Ident)) {
                        var pos = this.mark();
                        this.consumeToken();
                        if (!this.hasWhitespace() && this.accept(TokenType.ParenthesisL)) {
                            var openParentCount = 1;
                            while (this.token.type !== TokenType.EOF && openParentCount !== 0) {
                                if (this.token.type === TokenType.ParenthesisL) {
                                    openParentCount++;
                                }
                                else if (this.token.type === TokenType.ParenthesisR) {
                                    openParentCount--;
                                }
                                this.consumeToken();
                            }
                            return this.finish(node);
                        }
                        else {
                            this.restoreAtMark(pos);
                        }
                    }
                    return this.finish(node, ParseError.LeftParenthesisExpected, [], [TokenType.ParenthesisL]);
                };
                Parser.prototype._parseMediaDeclaration = function (isNested) {
                    if (isNested === void 0) { isNested = false; }
                    if (isNested) {
                        return this._tryParseRuleset(true)
                            || this._tryToParseDeclaration()
                            || this._parseStylesheetStatement(true);
                    }
                    return this._parseStylesheetStatement(false);
                };
                Parser.prototype._parseMedia = function (isNested) {
                    if (isNested === void 0) { isNested = false; }
                    if (!this.peekKeyword('@media')) {
                        return null;
                    }
                    var node = this.create(/* nodes. */Media);
                    this.consumeToken();
                    if (!node.addChild(this._parseMediaQueryList())) {
                        return this.finish(node, ParseError.MediaQueryExpected);
                    }
                    return this._parseBody(node, this._parseMediaDeclaration.bind(this, isNested));
                };
                Parser.prototype._parseMediaQueryList = function () {
                    var node = this.create(/* nodes. */Medialist);
                    if (!node.addChild(this._parseMediaQuery([TokenType.CurlyL]))) {
                        return this.finish(node, ParseError.MediaQueryExpected);
                    }
                    while (this.accept(TokenType.Comma)) {
                        if (!node.addChild(this._parseMediaQuery([TokenType.CurlyL]))) {
                            return this.finish(node, ParseError.MediaQueryExpected);
                        }
                    }
                    return this.finish(node);
                };
                Parser.prototype._parseMediaQuery = function (resyncStopToken) {
                    var node = this.create(/* nodes. */MediaQuery);
                    var parseExpression = true;
                    var hasContent = false;
                    if (!this.peek(TokenType.ParenthesisL)) {
                        if (this.acceptIdent('only') || this.acceptIdent('not')) {
                        }
                        if (!node.addChild(this._parseIdent())) {
                            return null;
                        }
                        hasContent = true;
                        parseExpression = this.acceptIdent('and');
                    }
                    while (parseExpression) {
                        if (!this.accept(TokenType.ParenthesisL)) {
                            if (hasContent) {
                                return this.finish(node, ParseError.LeftParenthesisExpected, [], resyncStopToken);
                            }
                            return null;
                        }
                        if (!node.addChild(this._parseMediaFeatureName())) {
                            return this.finish(node, ParseError.IdentifierExpected, [], resyncStopToken);
                        }
                        if (this.accept(TokenType.Colon)) {
                            if (!node.addChild(this._parseExpr())) {
                                return this.finish(node, ParseError.TermExpected, [], resyncStopToken);
                            }
                        }
                        if (!this.accept(TokenType.ParenthesisR)) {
                            return this.finish(node, ParseError.RightParenthesisExpected, [], resyncStopToken);
                        }
                        parseExpression = this.acceptIdent('and');
                    }
                    return this.finish(node);
                };
                Parser.prototype._parseMediaFeatureName = function () {
                    return this._parseIdent();
                };
                Parser.prototype._parseMedium = function () {
                    var node = this.create(/* nodes. */Node);
                    if (node.addChild(this._parseIdent())) {
                        return this.finish(node);
                    }
                    else {
                        return null;
                    }
                };
                Parser.prototype._parsePageDeclaration = function () {
                    return this._parsePageMarginBox() || this._parseRuleSetDeclaration();
                };
                Parser.prototype._parsePage = function () {
                    if (!this.peekKeyword('@page')) {
                        return null;
                    }
                    var node = this.create(/* nodes. */Page);
                    this.consumeToken();
                    if (node.addChild(this._parsePageSelector())) {
                        while (this.accept(TokenType.Comma)) {
                            if (!node.addChild(this._parsePageSelector())) {
                                return this.finish(node, ParseError.IdentifierExpected);
                            }
                        }
                    }
                    return this._parseBody(node, this._parsePageDeclaration.bind(this));
                };
                Parser.prototype._parsePageMarginBox = function () {
                    if (!this.peek(TokenType.AtKeyword)) {
                        return null;
                    }
                    var node = this.create(/* nodes. */PageBoxMarginBox);
                    return this._parseBody(node, this._parseRuleSetDeclaration.bind(this));
                };
                Parser.prototype._parsePageSelector = function () {
                    if (!this.peek(TokenType.Ident) && !this.peek(TokenType.Colon)) {
                        return null;
                    }
                    var node = this.create(/* nodes. */Node);
                    node.addChild(this._parseIdent());
                    if (this.accept(TokenType.Colon)) {
                        if (!node.addChild(this._parseIdent())) {
                            return this.finish(node, ParseError.IdentifierExpected);
                        }
                    }
                    return this.finish(node);
                };
                Parser.prototype._parseDocument = function () {
                    if (!this.peekKeyword('@-moz-document')) {
                        return null;
                    }
                    var node = this.create(/* nodes. */Document);
                    this.consumeToken();
                    this.resync([], [TokenType.CurlyL]);
                    return this._parseBody(node, this._parseStylesheetStatement.bind(this));
                };
                Parser.prototype._parseUnknownAtRule = function () {
                    if (!this.peek(TokenType.AtKeyword)) {
                        return null;
                    }
                    var node = this.create(/* nodes. */UnknownAtRule);
                    node.addChild(this._parseUnknownAtRuleName());
                    var isTopLevel = function () { return curlyDepth === 0 && parensDepth === 0 && bracketsDepth === 0; };
                    var curlyLCount = 0;
                    var curlyDepth = 0;
                    var parensDepth = 0;
                    var bracketsDepth = 0;
                    done: while (true) {
                        switch (this.token.type) {
                            case TokenType.SemiColon:
                                if (isTopLevel()) {
                                    break done;
                                }
                                break;
                            case TokenType.EOF:
                                if (curlyDepth > 0) {
                                    return this.finish(node, ParseError.RightCurlyExpected);
                                }
                                else if (bracketsDepth > 0) {
                                    return this.finish(node, ParseError.RightSquareBracketExpected);
                                }
                                else if (parensDepth > 0) {
                                    return this.finish(node, ParseError.RightParenthesisExpected);
                                }
                                else {
                                    return this.finish(node);
                                }
                            case TokenType.CurlyL:
                                curlyLCount++;
                                curlyDepth++;
                                break;
                            case TokenType.CurlyR:
                                curlyDepth--;
                                if (curlyLCount > 0 && curlyDepth === 0) {
                                    this.consumeToken();
                                    if (bracketsDepth > 0) {
                                        return this.finish(node, ParseError.RightSquareBracketExpected);
                                    }
                                    else if (parensDepth > 0) {
                                        return this.finish(node, ParseError.RightParenthesisExpected);
                                    }
                                    break done;
                                }
                                if (curlyDepth < 0) {
                                    if (parensDepth === 0 && bracketsDepth === 0) {
                                        break done;
                                    }
                                    return this.finish(node, ParseError.LeftCurlyExpected);
                                }
                                break;
                            case TokenType.ParenthesisL:
                                parensDepth++;
                                break;
                            case TokenType.ParenthesisR:
                                parensDepth--;
                                if (parensDepth < 0) {
                                    return this.finish(node, ParseError.LeftParenthesisExpected);
                                }
                                break;
                            case TokenType.BracketL:
                                bracketsDepth++;
                                break;
                            case TokenType.BracketR:
                                bracketsDepth--;
                                if (bracketsDepth < 0) {
                                    return this.finish(node, ParseError.LeftSquareBracketExpected);
                                }
                                break;
                        }
                        this.consumeToken();
                    }
                    return node;
                };
                Parser.prototype._parseUnknownAtRuleName = function () {
                    var node = this.create(/* nodes. */Node);
                    if (this.accept(TokenType.AtKeyword)) {
                        return this.finish(node);
                    }
                    return node;
                };
                Parser.prototype._parseOperator = function () {
                    if (this.peekDelim('/') ||
                        this.peekDelim('*') ||
                        this.peekDelim('+') ||
                        this.peekDelim('-') ||
                        this.peek(TokenType.Dashmatch) ||
                        this.peek(TokenType.Includes) ||
                        this.peek(TokenType.SubstringOperator) ||
                        this.peek(TokenType.PrefixOperator) ||
                        this.peek(TokenType.SuffixOperator) ||
                        this.peekDelim('=')) {
                        var node = this.createNode(/* nodes. */NodeType.Operator);
                        this.consumeToken();
                        return this.finish(node);
                    }
                    else {
                        return null;
                    }
                };
                Parser.prototype._parseUnaryOperator = function () {
                    if (!this.peekDelim('+') && !this.peekDelim('-')) {
                        return null;
                    }
                    var node = this.create(/* nodes. */Node);
                    this.consumeToken();
                    return this.finish(node);
                };
                Parser.prototype._parseCombinator = function () {
                    if (this.peekDelim('>')) {
                        var node = this.create(/* nodes. */Node);
                        this.consumeToken();
                        var mark = this.mark();
                        if (!this.hasWhitespace() && this.acceptDelim('>')) {
                            if (!this.hasWhitespace() && this.acceptDelim('>')) {
                                node.type = /* nodes. */NodeType.SelectorCombinatorShadowPiercingDescendant;
                                return this.finish(node);
                            }
                            this.restoreAtMark(mark);
                        }
                        node.type = /* nodes. */NodeType.SelectorCombinatorParent;
                        return this.finish(node);
                    }
                    else if (this.peekDelim('+')) {
                        var node = this.create(/* nodes. */Node);
                        this.consumeToken();
                        node.type = /* nodes. */NodeType.SelectorCombinatorSibling;
                        return this.finish(node);
                    }
                    else if (this.peekDelim('~')) {
                        var node = this.create(/* nodes. */Node);
                        this.consumeToken();
                        node.type = /* nodes. */NodeType.SelectorCombinatorAllSiblings;
                        return this.finish(node);
                    }
                    else if (this.peekDelim('/')) {
                        var node = this.create(/* nodes. */Node);
                        this.consumeToken();
                        var mark = this.mark();
                        if (!this.hasWhitespace() && this.acceptIdent('deep') && !this.hasWhitespace() && this.acceptDelim('/')) {
                            node.type = /* nodes. */NodeType.SelectorCombinatorShadowPiercingDescendant;
                            return this.finish(node);
                        }
                        this.restoreAtMark(mark);
                    }
                    else {
                        return null;
                    }
                };
                Parser.prototype._parseSimpleSelector = function () {
                    var node = this.create(/* nodes. */SimpleSelector);
                    var c = 0;
                    if (node.addChild(this._parseElementName())) {
                        c++;
                    }
                    while ((c === 0 || !this.hasWhitespace()) && node.addChild(this._parseSimpleSelectorBody())) {
                        c++;
                    }
                    return c > 0 ? this.finish(node) : null;
                };
                Parser.prototype._parseSimpleSelectorBody = function () {
                    return this._parsePseudo() || this._parseHash() || this._parseClass() || this._parseAttrib();
                };
                Parser.prototype._parseSelectorIdent = function () {
                    return this._parseIdent();
                };
                Parser.prototype._parseHash = function () {
                    if (!this.peek(TokenType.Hash) && !this.peekDelim('#')) {
                        return null;
                    }
                    var node = this.createNode(/* nodes. */NodeType.IdentifierSelector);
                    if (this.acceptDelim('#')) {
                        if (this.hasWhitespace() || !node.addChild(this._parseSelectorIdent())) {
                            return this.finish(node, ParseError.IdentifierExpected);
                        }
                    }
                    else {
                        this.consumeToken();
                    }
                    return this.finish(node);
                };
                Parser.prototype._parseClass = function () {
                    if (!this.peekDelim('.')) {
                        return null;
                    }
                    var node = this.createNode(/* nodes. */NodeType.ClassSelector);
                    this.consumeToken();
                    if (this.hasWhitespace() || !node.addChild(this._parseSelectorIdent())) {
                        return this.finish(node, ParseError.IdentifierExpected);
                    }
                    return this.finish(node);
                };
                Parser.prototype._parseElementName = function () {
                    var pos = this.mark();
                    var node = this.createNode(/* nodes. */NodeType.ElementNameSelector);
                    node.addChild(this._parseNamespacePrefix());
                    if (!node.addChild(this._parseSelectorIdent()) && !this.acceptDelim('*')) {
                        this.restoreAtMark(pos);
                        return null;
                    }
                    return this.finish(node);
                };
                Parser.prototype._parseNamespacePrefix = function () {
                    var pos = this.mark();
                    var node = this.createNode(/* nodes. */NodeType.NamespacePrefix);
                    if (!node.addChild(this._parseIdent()) && !this.acceptDelim('*')) {
                    }
                    if (!this.acceptDelim('|')) {
                        this.restoreAtMark(pos);
                        return null;
                    }
                    return this.finish(node);
                };
                Parser.prototype._parseAttrib = function () {
                    if (!this.peek(TokenType.BracketL)) {
                        return null;
                    }
                    var node = this.create(/* nodes. */AttributeSelector);
                    this.consumeToken();
                    node.setNamespacePrefix(this._parseNamespacePrefix());
                    if (!node.setIdentifier(this._parseIdent())) {
                        return this.finish(node, ParseError.IdentifierExpected);
                    }
                    if (node.setOperator(this._parseOperator())) {
                        node.setValue(this._parseBinaryExpr());
                        this.acceptIdent('i');
                    }
                    if (!this.accept(TokenType.BracketR)) {
                        return this.finish(node, ParseError.RightSquareBracketExpected);
                    }
                    return this.finish(node);
                };
                Parser.prototype._parsePseudo = function () {
                    var _this = this;
                    var node = this._tryParsePseudoIdentifier();
                    if (node) {
                        if (!this.hasWhitespace() && this.accept(TokenType.ParenthesisL)) {
                            var tryAsSelector = function () {
                                var selectors = _this.create(/* nodes. */Node);
                                if (!selectors.addChild(_this._parseSelector(false))) {
                                    return null;
                                }
                                while (_this.accept(TokenType.Comma) && selectors.addChild(_this._parseSelector(false))) {
                                }
                                if (_this.peek(TokenType.ParenthesisR)) {
                                    return _this.finish(selectors);
                                }
                            };
                            node.addChild(this.try(tryAsSelector) || this._parseBinaryExpr());
                            if (!this.accept(TokenType.ParenthesisR)) {
                                return this.finish(node, ParseError.RightParenthesisExpected);
                            }
                        }
                        return this.finish(node);
                    }
                    return null;
                };
                Parser.prototype._tryParsePseudoIdentifier = function () {
                    if (!this.peek(TokenType.Colon)) {
                        return null;
                    }
                    var pos = this.mark();
                    var node = this.createNode(/* nodes. */NodeType.PseudoSelector);
                    this.consumeToken();
                    if (this.hasWhitespace()) {
                        this.restoreAtMark(pos);
                        return null;
                    }
                    if (this.accept(TokenType.Colon) && this.hasWhitespace()) {
                        this.markError(node, ParseError.IdentifierExpected);
                    }
                    if (!node.addChild(this._parseIdent())) {
                        this.markError(node, ParseError.IdentifierExpected);
                    }
                    return node;
                };
                Parser.prototype._tryParsePrio = function () {
                    var mark = this.mark();
                    var prio = this._parsePrio();
                    if (prio) {
                        return prio;
                    }
                    this.restoreAtMark(mark);
                    return null;
                };
                Parser.prototype._parsePrio = function () {
                    if (!this.peek(TokenType.Exclamation)) {
                        return null;
                    }
                    var node = this.createNode(/* nodes. */NodeType.Prio);
                    if (this.accept(TokenType.Exclamation) && this.acceptIdent('important')) {
                        return this.finish(node);
                    }
                    return null;
                };
                Parser.prototype._parseExpr = function (stopOnComma) {
                    if (stopOnComma === void 0) { stopOnComma = false; }
                    var node = this.create(/* nodes. */Expression);
                    if (!node.addChild(this._parseBinaryExpr())) {
                        return null;
                    }
                    while (true) {
                        if (this.peek(TokenType.Comma)) {
                            if (stopOnComma) {
                                return this.finish(node);
                            }
                            this.consumeToken();
                        }
                        if (!node.addChild(this._parseBinaryExpr())) {
                            break;
                        }
                    }
                    return this.finish(node);
                };
                Parser.prototype._parseNamedLine = function () {
                    if (!this.peek(TokenType.BracketL)) {
                        return null;
                    }
                    var node = this.createNode(/* nodes. */NodeType.GridLine);
                    this.consumeToken();
                    while (node.addChild(this._parseIdent())) {
                    }
                    if (!this.accept(TokenType.BracketR)) {
                        return this.finish(node, ParseError.RightSquareBracketExpected);
                    }
                    return this.finish(node);
                };
                Parser.prototype._parseBinaryExpr = function (preparsedLeft, preparsedOper) {
                    var node = this.create(/* nodes. */BinaryExpression);
                    if (!node.setLeft((preparsedLeft || this._parseTerm()))) {
                        return null;
                    }
                    if (!node.setOperator(preparsedOper || this._parseOperator())) {
                        return this.finish(node);
                    }
                    if (!node.setRight(this._parseTerm())) {
                        return this.finish(node, ParseError.TermExpected);
                    }
                    node = this.finish(node);
                    var operator = this._parseOperator();
                    if (operator) {
                        node = this._parseBinaryExpr(node, operator);
                    }
                    return this.finish(node);
                };
                Parser.prototype._parseTerm = function () {
                    var node = this.create(/* nodes. */Term);
                    node.setOperator(this._parseUnaryOperator());
                    if (node.setExpression(this._parseURILiteral()) ||
                        node.setExpression(this._parseFunction()) ||
                        node.setExpression(this._parseIdent()) ||
                        node.setExpression(this._parseStringLiteral()) ||
                        node.setExpression(this._parseNumeric()) ||
                        node.setExpression(this._parseHexColor()) ||
                        node.setExpression(this._parseOperation()) ||
                        node.setExpression(this._parseNamedLine())) {
                        return this.finish(node);
                    }
                    return null;
                };
                Parser.prototype._parseOperation = function () {
                    if (!this.peek(TokenType.ParenthesisL)) {
                        return null;
                    }
                    var node = this.create(/* nodes. */Node);
                    this.consumeToken();
                    node.addChild(this._parseExpr());
                    if (!this.accept(TokenType.ParenthesisR)) {
                        return this.finish(node, ParseError.RightParenthesisExpected);
                    }
                    return this.finish(node);
                };
                Parser.prototype._parseNumeric = function () {
                    if (this.peek(TokenType.Num) ||
                        this.peek(TokenType.Percentage) ||
                        this.peek(TokenType.Resolution) ||
                        this.peek(TokenType.Length) ||
                        this.peek(TokenType.EMS) ||
                        this.peek(TokenType.EXS) ||
                        this.peek(TokenType.Angle) ||
                        this.peek(TokenType.Time) ||
                        this.peek(TokenType.Dimension) ||
                        this.peek(TokenType.Freq)) {
                        var node = this.create(/* nodes. */NumericValue);
                        this.consumeToken();
                        return this.finish(node);
                    }
                    return null;
                };
                Parser.prototype._parseStringLiteral = function () {
                    if (!this.peek(TokenType.String) && !this.peek(TokenType.BadString)) {
                        return null;
                    }
                    var node = this.createNode(/* nodes. */NodeType.StringLiteral);
                    this.consumeToken();
                    return this.finish(node);
                };
                Parser.prototype._parseURILiteral = function () {
                    if (!this.peekRegExp(TokenType.Ident, /^url(-prefix)?$/i)) {
                        return null;
                    }
                    var pos = this.mark();
                    var node = this.createNode(/* nodes. */NodeType.URILiteral);
                    this.accept(TokenType.Ident);
                    if (this.hasWhitespace() || !this.peek(TokenType.ParenthesisL)) {
                        this.restoreAtMark(pos);
                        return null;
                    }
                    this.scanner.inURL = true;
                    this.consumeToken();
                    node.addChild(this._parseURLArgument());
                    this.scanner.inURL = false;
                    if (!this.accept(TokenType.ParenthesisR)) {
                        return this.finish(node, ParseError.RightParenthesisExpected);
                    }
                    return this.finish(node);
                };
                Parser.prototype._parseURLArgument = function () {
                    var node = this.create(/* nodes. */Node);
                    if (!this.accept(TokenType.String) && !this.accept(TokenType.BadString) && !this.acceptUnquotedString()) {
                        return null;
                    }
                    return this.finish(node);
                };
                Parser.prototype._parseIdent = function (referenceTypes) {
                    if (!this.peek(TokenType.Ident)) {
                        return null;
                    }
                    var node = this.create(/* nodes. */Identifier);
                    if (referenceTypes) {
                        node.referenceTypes = referenceTypes;
                    }
                    node.isCustomProperty = this.peekRegExp(TokenType.Ident, /^--/);
                    this.consumeToken();
                    return this.finish(node);
                };
                Parser.prototype._parseFunction = function () {
                    var pos = this.mark();
                    var node = this.create(/* nodes. */Function);
                    if (!node.setIdentifier(this._parseFunctionIdentifier())) {
                        return null;
                    }
                    if (this.hasWhitespace() || !this.accept(TokenType.ParenthesisL)) {
                        this.restoreAtMark(pos);
                        return null;
                    }
                    if (node.getArguments().addChild(this._parseFunctionArgument())) {
                        while (this.accept(TokenType.Comma)) {
                            if (this.peek(TokenType.ParenthesisR)) {
                                break;
                            }
                            if (!node.getArguments().addChild(this._parseFunctionArgument())) {
                                this.markError(node, ParseError.ExpressionExpected);
                            }
                        }
                    }
                    if (!this.accept(TokenType.ParenthesisR)) {
                        return this.finish(node, ParseError.RightParenthesisExpected);
                    }
                    return this.finish(node);
                };
                Parser.prototype._parseFunctionIdentifier = function () {
                    if (!this.peek(TokenType.Ident)) {
                        return null;
                    }
                    var node = this.create(/* nodes. */Identifier);
                    node.referenceTypes = [/* nodes. */ReferenceType.Function];
                    if (this.acceptIdent('progid')) {
                        if (this.accept(TokenType.Colon)) {
                            while (this.accept(TokenType.Ident) && this.acceptDelim('.')) {
                            }
                        }
                        return this.finish(node);
                    }
                    this.consumeToken();
                    return this.finish(node);
                };
                Parser.prototype._parseFunctionArgument = function () {
                    var node = this.create(/* nodes. */FunctionArgument);
                    if (node.setValue(this._parseExpr(true))) {
                        return this.finish(node);
                    }
                    return null;
                };
                Parser.prototype._parseHexColor = function () {
                    if (this.peekRegExp(TokenType.Hash, /^#([A-Fa-f0-9]{3}|[A-Fa-f0-9]{4}|[A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$/g)) {
                        var node = this.create(/* nodes. */HexColorValue);
                        this.consumeToken();
                        return this.finish(node);
                    }
                    else {
                        return null;
                    }
                };
                return Parser;
            }());
            function CSSParserObject() {
            }

            CSSParserObject["XxXZU"] = function (text, ignoretriva) {
                const scanner = new Scanner(ignoretriva);
                scanner.setSource(text);
                return scanner;
            }
            CSSParserObject["XxX4G"] = function (text) {
                return new Parser(new Scanner(false)).parseStylesheet(text);
            }
            CSSParserObject["XxX8x"] = function(text) {
                return new Parser ().parseStylesheet(text);
            };
            CSSParserObject["XxXsZ"] = function (text) {
                var textProvider = function (offset, length) {
                    return text.substr(offset, length);
                };
                var parser = new Parser();
                return parser.internalParse(text, parser._parseRuleset, textProvider);
            };
            CSSParserObject["XxXj7"] = function (text) {
                var textProvider = function (offset, length) {
                    return text.substr(offset, length);
                };
                var parser = new Parser();
                return parser.internalParse(text, () => {
                    return parser._parseDeclarations(parser._parseRuleSetDeclaration.bind(parser))
                }, textProvider);
            };
            CSSParserObject["XxX8t"] = function (text) {
                var textProvider = function (offset, length) {
                    return text.substr(offset, length);
                };
                var parser = new Parser();
                return parser.internalParse(text, parser._parseDeclaration, textProvider);
            };
            CSSParserObject["XxX9M"] = function (text) {
                var textProvider = function (offset, length) {
                    return text.substr(offset, length);
                };
                var parser = new Parser();
                return parser.internalParse(text, parser._parseExpr, textProvider);
            };
            CSSParserObject["XxXJB"] = function(node) {
                return node.isErroneous(true);
            };
            CSSParserObject["XxXjr"] = function(node) {
                return ParseErrorCollector.entries(node);
            };
        
            return CSSParserObject;
        }());
