package com.zhenxiang.nyaa.view

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import com.zhenxiang.nyaa.BuildConfig
import org.commonmark.Extension
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.AttributeProvider
import org.commonmark.renderer.html.HtmlRenderer

class MarkdownWebView: WebView {

    private val extensions: List<Extension> = listOf(AutolinkExtension.create(), TablesExtension.create())
    private val parser: Parser = Parser.builder().extensions(extensions).build()
    private val renderer = HtmlRenderer.builder()
        .extensions(extensions)
        .attributeProviderFactory { NyaaAttributesProvider() }
        .build()

    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int): super(context, attrs, defStyle)

    init {
        if (BuildConfig.DEBUG) {
            setWebContentsDebuggingEnabled(true)
        }
    }

    fun loadMarkdown(data: String) {
        val document: Node = parser.parse(data)
        super.loadDataWithBaseURL("file:///android_asset/",
            "<head>${getHtmlForCSSAsset("bootstrap.css")}" +
                    "${getHtmlForCSSAsset("nyaa.css")}</head>" +
                    "<body id=\"torrent-description\">${renderer.render(document)}</body>",
            null, null, null)
    }

    private fun getHtmlForCSSAsset(path: String): String {
        return "<link href=\"file:///android_asset/$path\" type=\"text/css\" rel=\"stylesheet\"/>"
    }
}

class NyaaAttributesProvider: AttributeProvider {
    override fun setAttributes(
        node: Node?,
        tagName: String?,
        attributes: MutableMap<String, String>?
    ) {
        attributes?.let { _ ->
            if (node is TableBlock) {
                attributes["class"] = "table table-striped table-bordered"
                attributes["style"] = "width: auto"
            }
        }
    }
}
