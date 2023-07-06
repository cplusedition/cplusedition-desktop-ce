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
package sf.andrians.cplusedition.support.templates

import com.cplusedition.bot.core.TextUt
import com.cplusedition.bot.dsl.html.api.IElement
import com.cplusedition.bot.dsl.html.impl.Html5Builder
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An
import sf.andrians.cplusedition.support.IStorage
import sf.andrians.cplusedition.support.Support
import java.io.Writer
import java.net.URI
import java.net.URISyntaxException

object Templates {

    open class TemplateBase : Html5Builder() {
        companion object {
            fun viewMediaHref(cpath: String?): String {
                return try {
                    val uri = URI(null, null, null, -1, cpath, null, null)
                    "href=\"" + uri.rawPath + "?view\""
                } catch (e: URISyntaxException) {
                    
                    ""
                }
            }
        }
    }

    class Error404Template : Html5Builder() {
        fun build(storage: IStorage, writable: Boolean, opath: String): IElement {
            val res = storage.rsrc
            val notfound = res.get(R.string.NotFound)
            var magic = ""
            if (writable) {
                magic = TextUt.format("<div class=\"fa fa-magic\" style=\"margin: 0 0.5em;color:#08c\" %s=\"%s\"></div>",
                    An.ATTR.xPageTemplate,
                    opath)
            }
            val timestamp = storage.getCustomResourcesTimestamp()
            return fragment(
                doctype(),
                html(
                    head(
                        contenttype("text/html; charset=UTF-8"),
                        meta(name("viewport"),
                            super.content("width=device-width, height=device-height, initial-scale=1.0, user-scalable=no")),
                        link(rel("stylesheet"), type("text/css"), href("/" + An.PATH.assetsClientCss + "?t=" + timestamp))),
                    body(
                        istyle("background-color:#ffffff;background-image:url(/assets/images/res/cloud1024c.png);background-repeat: repeat;"),
                        div(
                            css(An.CSS.xRoot),
                            div(
                                istyle(
                                    "border: 1px solid rgb(136, 136, 136); border-radius: 5px; box-shadow: rgb(136, 136, 136) 0px 0px 10px; margin: 20px auto; padding: 0px 10px 10px 10px; text-align: center; max-width: min(32em, 75vw); background-color: rgb(255, 193, 37);"),
                                div(istyle("font-size:2rem;font-weight:bold;color:#000;margin:10px auto 10px auto;"),
                                    css("x-symbol fa fa-warning")),
                                div(
                                    istyle(
                                        "border-radius: 5px; font-family: RobotoCondensed-Regular; font-size:1rem;padding: 10px; background-color: rgb(255, 236, 139);overflow:auto;white-space:nowrap;word-wrap:break-word;"),
                                    div(
                                        css("x-title"),
                                        istyle("font-family:JotiOne-Regular;font-size:1.25rem;font-weight:bold;margin:0px 10px 5px 10px;"),
                                        notfound,
                                        raw(magic)),
                                    div(css("x-msg"),
                                        span(istyle("margin:0 1rem"),
                                            esc(opath)))))))))
        }
    }

    class Error500Template : Html5Builder() {
        fun build(storage: IStorage, opath: String, msg: String?): IElement {
            val res = storage.rsrc
            val timestamp = storage.getCustomResourcesTimestamp()
            return fragment(
                doctype(),
                html(
                    head(
                        contenttype("text/html; charset=UTF-8"),
                        meta(name("viewport"),
                            super.content("width=device-width, height=device-height, initial-scale=1.0, user-scalable=no")),
                        link(rel("stylesheet"), type("text/css"), href("/" + An.PATH.assetsClientCss + "?t=" + timestamp))),
                    body(
                        istyle("background-color:#ffffff;background-image:url(/assets/images/res/cloud1024c.png);background-repeat: repeat;"),
                        div(
                            css(An.CSS.xRoot),
                            div(
                                istyle(
                                    "border: 1px solid rgb(136, 136, 136); border-radius: 5px; box-shadow: rgb(136, 136, 136) 0px 0px 10px; margin: 20px auto; padding: 0px 10px 10px 10px; text-align: center; max-width: min(32em, 75vw); background-color: rgb(178, 34, 34);"),
                                div(istyle("font-size:2rem;font-weight:bold;color:#fff;margin:10px auto 10px auto;"),
                                    css("x-symbol fa fa-exclamation-circle")),
                                div(
                                    istyle(
                                        "border-radius: 5px; font-family: RobotoCondensed-Regular; font-size:1rem; padding: 2ex 1em; background-color: rgb(244,99,71);overflow:auto;white-space:nowrap;word-wrap:break-word;"),
                                    div(
                                        css("x-title"),
                                        istyle("font-family:JotiOne-Regular;font-size:1.25rem;font-weight:bold;padding:0px 0 5px 0;"),
                                        span(istyle("margin:0 1rem"),
                                            msg ?: res.get(R.string.Error))),
                                    div(css("x-msg"),
                                        span(istyle("margin:0 1rem"),
                                            esc(opath)))))))))
        }
    }

    class HomeTemplate {
        fun serialize(ret: Writer, storage: IStorage, spinner: String, settings: String) {
            val timestamp = storage.getCustomResourcesTimestamp()
            val testjs = ""
            ret.append("""<!DOCTYPE html>
<html>
		<head>
				<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
				<meta name="viewport" content="width=device-width,height=device-height,initial-scale=1.0,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no">
				<link rel="stylesheet" type="text/css" href="${storage.getHostCss()}?t=$timestamp">
				<script type="text/javascript" title="settings">$settings</script>
				<script type="text/javascript" src="assets/js/r.js"></script>
				<script type="text/javascript" src="assets/js/cssparser.js"></script>
		$testjs</head>
		<body>
				<div id="${An.ID.splash}" class="${An.CSS.AnSmokescreen}" style="display:flex; align-items:center; justify-content:center; opacity:0.25; width:100vw; height:100vh; z-index:9999;">
						<div class="${An.CSS.AnSpinner}">${spinner}</div>
				</div>
				<div id="${An.ID.toolbar}">
						<div class="${An.CSS.AnToolbar}" style="visibility:visible;"></div>
				</div>
				<div id="${An.ID.content}">
						<iframe id="${An.ID.contentIFrame}" sandbox="allow-same-origin allow-scripts allow-forms" allowfullscreen seamless="true" border="0" style="background-color:#fff; border:0;"></iframe>
				</div>
				<div id="${An.ID.accessories}"></div>
				<div id="${An.ID.sidebar}"></div>
				<div id="${An.ID.sidepanel}"></div>
				<script type="text/javascript" src="assets/js/m.js"></script>
		</body>
</html>
""")
        }
    }

    class ImageTemplate : Html5Builder() {
        fun build(csspath: String, srcpath: String): IElement {
            return fragment(
                doctype(),
                html(
                    head(
                        contenttype("text/html; charset=UTF-8"),
                        meta(name("viewport"),
                            super.content("width=device-width, height=device-height, initial-scale=1.0, user-scalable=no")),
                        stylesheet(csspath)),
                    body(
                        div(css(An.CSS.xRoot),
                            div(id(An.ID.xViewer),
                                div(
                                    img(id(An.ID.xContent), src(srcpath))))))))
        }
    }

    class CSSEditorTemplate : Html5Builder() {
        fun build(storage: IStorage): IElement {
            val timestamp = storage.getCustomResourcesTimestamp()
            return fragment(
                doctype(),
                html(
                    head(
                        contenttype("text/html; charset=UTF-8"),
                        meta(name("viewport"),
                            content("width=device-width, height=device-height, initial-scale=1.0, user-scalable=no")),
                        stylesheet(An.PATH._assetsCSSEditorCss + "?t=" + timestamp)),
                    body(
                        textarea(id(An.ID.csseditor)),
                        script(type("text/javascript"), src(An.PATH._assetsCSSEditorJs)))))
        }
    }

    class TextTemplate : Html5Builder() {
        fun build(csspath: String, content: String): IElement {
            return fragment(
                doctype(),
                html(
                    head(
                        contenttype("text/html; charset=UTF-8"),
                        meta(
                            name("viewport"),
                            super.content("width=device-width, height=device-height, initial-scale=1.0, user-scalable=no")
                        ),
                        stylesheet(csspath)
                    ),
                    body(
                        div(css(An.CSS.xRoot),
                            div(id(An.ID.xViewer),
                                div(id(An.ID.xContent),
                                    pre(istyle(
                                        "margin:0;padding:20px 20px 75vh 20px;width:100vw;height:100vh;" +
                                                "box-sizing:border-box;overflow:auto;-webkit-overflow-scrolling:touch;"),
                                        esc(content))))))))
        }
    }

}
