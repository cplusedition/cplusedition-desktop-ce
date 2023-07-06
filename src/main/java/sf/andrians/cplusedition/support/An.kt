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
package sf.andrians.cplusedition.support

public abstract class An {
    object ATTR {
        const val AnAction = "xxx-i5"
        const val AnIndex = "xxx-as"
        const val AnInfo = "xxx-h5"
        const val AnMime = "xxx-j2"
        const val AnNoClick = "xxx-0p"
        const val AnOptions = "xxx-8f"
        const val AnRedoing = "xxx-1l"
        const val AnSaved = "xxx-lg"
        const val AnToggleGroup = "xxx-80"
        const val AnUndoing = "xxx-jk"
        const val autocapitalize = "autocapitalize"
        const val autocomplete = "autocomplete"
        const val autocorrect = "autocorrect"
        const val classes = "class"
        const val contenteditable = "contenteditable"
        const val href = "href"
        const val id = "id"
        const val name = "name"
        const val placeholder = "placeholder"
        const val spellcheck = "spellcheck"
        const val src = "src"
        const val style = "style"
        const val tabindex = "tabindex"
        const val xAction = "x-action"
        const val xAnnotation = "x-a"
        const val xButton = "x-bt"
        const val xDateFormat = "x-f"
        const val xInfo = "x-i"
        const val xPageTemplate = "x-pt"
        const val xPlaceholder = "x-ph"
        const val xRole = "x-role"
        const val xTemplate = "x-tp"
        const val xTemplatePlaceholder = "x-th"
        const val xTooltips = "x-t"
    }

    object AVALUE {
        const val xStop = "stop"
    }

    object AjaxAction {
        const val ajax = "XxX6e"
        const val audioStatusChanged = "XxXy9"
        const val backup = "XxXMs"
        const val browse = "XxXsa"
        const val export = "XxX30"
        const val gesture = "XxXPq"
        const val hideCaret = "XxXN1"
        const val login = "XxXDJ"
        const val loginResult = "XxXFv"
        const val navigateBack = "XxX8V"
        const val onCallback = "XxXoO"
        const val onNotificationResponse = "XxXED"
        const val onResizeWindow = "XxXxI"
        const val pause = "XxXGE"
        const val refreshEvents = "XxX6y"
        const val restore = "XxX0G"
        const val resume = "XxXAd"
        const val scanDocumentResult = "XxXiW"
        const val showIncoming = "XxXr9"
        const val showSidepanel = "XxXjH"
        const val spinner = "XxXww"
        const val spinnerHide = "XxXZZ"
        const val spinnerShow = "XxXdH"
        const val takePhotoResult = "XxXA6"
        const val testRunner = "testRunner"
        const val toaster = "XxXcg"
        const val toasterOnLoad = "XxXtol"
        const val videoRecordingResult = "XxXpu"
    }

    object CSS {
        const val AnButton = "XxXH4"
        const val AnSmokescreen = "XxXTT"
        const val AnSpin = "XxXdn"
        const val AnSpinner = "XxXV9"
        const val AnToolbar = "XxXoy"
        const val xAudio = "x-audio"
        const val xAudioChannels = "x-audio-channels"
        const val xAudioDatetime = "x-audio-datetime"
        const val xAudioDuration = "x-audio-duration"
        const val xAudioRate = "x-audio-rate"
        const val xBody = "x-body"
        const val xHeader = "x-header"
        const val xLandscape = "x-landscape"
        const val xMatchA = "x-m-a"
        const val xMatchT = "x-m-t"
        const val xPortrait = "x-portrait"
        const val xRoot = "x-root"
        const val xVideo = "x-video"
        const val xVideoDatetime = "x-video-datetime"
        const val xVideoDuration = "x-video-duration"
        const val xVideoPoster = "x-video-poster"
        const val xVideoResolution = "x-video-resolution"
        const val xxTop = "xx-top"
        const val xxScrollH = "xx-scroll-h"
        const val xxScrollV = "xx-scroll-v"
        const val xPlaceholder = "x-placeholder"
        const val xRightSidebarTab = "x-rsb-tab"
    }

    object ClientAction {
        const val bg = "bg/"
        const val classes = "classes/"
        const val cycle = "c/"
        const val cycle1 = "C/"
        const val hide = "hide/"
        const val move = "m/"
        const val radio = "r/"
        const val show = "show/"
        const val style = "s/"
        const val toggle = "T/"
    }

    object DEF {
        const val CORRECTED = ".~c~"
        const val FontAwesome = "FontAwesome"
        const val NOOP = "NOOP"
        const val System = "System"
        const val alarmMargin = 500
        const val backupKeyAlias = "#self"
        const val backupKeyPrefix = "#"
        const val dailyBackupDir = "~DailyBackup~"
        const val databaseKeyAlias = "#db"
        const val dialogBorderWidth = 10
        const val dialogMaxWidthLarge = 12
        const val dialogMaxWidthMedium = 9
        const val dialogMaxWidthSmall = 7
        const val dialogMaxWidthXLarge = 16
        const val dialogPaddingWidth = 10
        const val dragBorderDivider = 2
        const val filepickerHistorySize = 16
        const val filepickerMaxWidth = 640
        const val galleryItemThreshold = 24
        const val imageDimensionSnap = 128
        const val jpegQualityHigh = 95
        const val jpegQuality = 90
        const val jpegQualityThumbnail = 85
        const val jpegQualityLow = 75
        const val jpegQualityVeryLow = 60
        const val keepLongerDays = 7
        const val keepLongerSizeLimit = 16777216
        const val keepShorterDays = 1
        const val lightBorder = "1px solid rgba(0, 0, 0, 0.25)"
        const val maxBarcodeScale = 10
        const val maxClientActionClassesLength = 20
        const val maxLongToastLen = 256
        const val maxNodeInfoLen = 128
        const val maxOutputImageArea = 67108864
        const val maxToastLen = 64
        const val nobackup = "~nobackup~"
        const val opacityDisabled = 0.25
        const val opacityEnabled = 1.0
        const val opacityOpaque = 1.0
        const val opacityResizing = 0.1
        const val opacityShowBackground = 0.35
        const val opacitySmoke = 0.5
        const val opacitySpinner = 0.75
        const val opacityTransparent = 0.0
        const val pdfPosterSize = 1024
        const val pdfDpi = 300
        const val pdfScale = 4.1666
        const val previewPhotoSize = 1024
        const val recentFilePositionCount = 50
        const val recentSymbolCount = 100
        const val scrollableThumbnailSize = 512
        const val sidepanelMarginBottom = 10
        const val sidepanelWidth = 450
        const val symbolFontSizeRatio = 0.45
        const val templateBlank = "/assets/templates/blank/blank.html"
        const val themeBorder = "1px solid rgba(0, 0, 0, 0.5)"
        const val themeBorderRadius = "0.35rem"
        const val themeBorderWidth = 1.0
        const val themeBoxShadow = "2px 2px 10px rgba(0,0,0,0.5)"
        const val thumbnailSize = 512
        const val thumbnailSizeMicro = 96
        const val thumbnailThreshold = 524288
        const val thumbnailUsePool = 10
        const val tmpBackupSuffix = "~tmp~.tmp"
        const val toolbarFontSizeRatio = 0.5
        const val trashAutoCleanupInterval = 86400000
        const val webkitPrefix = "-webkit-"
        const val xmlNameChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-"
        const val zindexClientAnnotation = 1300
        const val zindexClientSearchResult = 1200
        const val zindexClientShow = 500
        const val zindexClientSidebar = 1800
        const val zindexClientSidepanel = 1100
        const val zindexClientStickerOnDocument = 50
        const val zindexClientStickerOnScreen = 75
        const val zindexConfirm = 4900
        const val zindexContextmenu = 4000
        const val zindexImageAnnotator = 3000
        const val zindexMax = 9999
        const val zindexPrompt = 4100
        const val zindexPromptSave = 2500
        const val zindexResizer = 6000
        const val zindexSidebar = 2000
        const val zindexSidepanel = 2100
        const val zindexToaster = 5000
        const val zindexToasterError = 5200
        const val zindexToasterWarning = 5100
        const val zindexToolbar = 1990
        const val zoomDelta = 2.0
        const val zoomScale = 0.01
    }

    object Effect {
        const val GRAY16 = 4
        const val GRAY2 = 1
        const val GRAY256 = 5
        const val GRAY4 = 2
        const val GRAY8 = 3
        const val NONE = 0
    }

    object EventKey {
        const val Color = "color"
        const val Description = "desc"
        const val Done = "done"
        const val DoneMs = "donems"
        const val DoneDesc = "donedesc"
        const val Events = "A"
        const val Exists = "exists"
        const val Id = "id"
        const val Lastms = "lastms"
        const val Ms = "ms"
        const val Pending = "pending"
        const val Private = "private"
        const val Repeat = "repeat"
        const val Serial = "S"
        const val Url = "url"
        const val Version = "V"
    }

    object EventRepeat {
        const val Daily = "Daily"
        const val Monthly = "Monthly"
        const val Off = "Off"
        const val Once = "Once"
        const val Weekly = "Weekly"
        const val Workdays = "Workdays"
        const val Yearly = "Yearly"
        val values = arrayOf("Off", "Once", "Daily", "Workdays", "Weekly", "Monthly", "Yearly")
    }

    object FilepickerCmd {
        const val COPY = 9
        const val COPY_INFO = 10
        const val DELETE = 11
        const val DELETE_ALL = 14
        const val DELETE_DIRSUBTREE = 13
        const val DELETE_EMPTY_DIRS = 20
        const val DELETE_INFO = 15
        const val DIRINFO = 23
        const val FILEINFO = 1
        const val FILEINFOS = 22
        const val INVALID = 0
        const val LISTDIR = 4
        const val LIST_RECURSIVE = 21
        const val LOCAL_IMAGE_INFOS = 17
        const val LOCAL_IMAGE_THUMBNAILS = 18
        const val MKDIRS = 7
        const val RENAME = 8
        const val SHRED = 24
    }

    object FontCategories {
        const val All = "All"
        const val Decorative = "Decorative"
        const val Monospace = "Monospace"
        const val Others = "Others"
        const val SansSerif = "Sans Serif"
        const val Serif = "Serif"
        const val System = "System"
    }

    object FontInfoKey {
        const val category = "category"
        const val fontfaceformat = "fontfaceformat"
        const val fontname = "fontname"
        const val glyphcount = "glyphcount"
        const val glyphnames = "glyphnames"
        const val glyphs = "glyphs"
        const val license = "license"
        const val size = "size"
        const val styles = "styles"
        const val subsets = "subsets"
        const val url = "url"
    }

    object GestureEvent {
        const val leftInward = "li"
        const val leftOutward = "lo"
        const val pinchZoom = "pz"
        const val rightInward = "ri"
        const val rightOutward = "ro"
    }

    object GestureKey {
        const val event = "e"
        const val y = "y"
    }

    object ID {
        const val accessories = "XxXs5"
        const val content = "XxXFP"
        const val contentIFrame = "XxXci"
        const val contextmenuSelect = "XxXhL"
        const val csseditor = "code"
        const val focusbox = "XxXqS"
        const val searchResult = "XxXhK"
        const val sidebar = "XxX16"
        const val sidebarDocument = "XxXdW"
        const val sidebarExplore = "XxXgA"
        const val sidebarResize = "XxXGz"
        const val sidebarTemplates = "XxXT1"
        const val sidepanel = "XxXO1"
        const val splash = "XxXk4"
        const val toolbar = "XxX0V"
        const val xContent = "x-content"
        const val xLeftSidepanel = "x-leftsidepanel"
        const val xLeftSplitpanel = "x-leftsplitpanel"
        const val xMarkers = "x-markers"
        const val xOpenLeftSidepanel = "x-open-leftsidepanel"
        const val xOpenRightSidepanel = "x-open-rightsidepanel"
        const val xRightSidebar = "x-rightsidebar"
        const val xRightSidepanel = "x-rightsidepanel"
        const val xRightSplitpanel = "x-rightsplitpanel"
        const val xViewer = "x-viewer"
    }

    object Key {
        const val quality = "XxXqty"
        const val attrs = "XxXet"
        const val backward = "XxXSG"
        const val baseurl = "XxX5h"
        const val busy = "XxXq4"
        const val checksum = "XxXmq"
        const val copying = "XxXEI"
        const val count = "XxXiI"
        const val cpath = "XxXCa"
        const val css = "XxXcss"
        const val cssrule = "XxXJ8"
        const val cut = "XxXS4"
        const val data = "XxXXD"
        const val deleting = "XxXU0"
        const val dimension = "XxXdb"
        const val dirinfo = "XxXnr"
        const val dirpath = "XxXtB"
        const val dirtree = "XxXi5"
        const val dst = "XxXkZ"
        const val element = "XxXqh"
        const val errors = "XxXox"
        const val expectedfailure = "XxXNT"
        const val expectedresult = "XxXIS"
        const val expire = "XxX5i"
        const val fails = "XxX6v"
        const val fileinfo = "XxXML"
        const val filename = "XxXdN"
        const val filestat = "XxXWQ"
        const val filter = "XxXOC"
        const val filterIgnorecase = "XxXT5"
        const val forward = "XxXhP"
        const val free = "XxXSM"
        const val height = "XxXiE"
        const val hold = "XxXx2"
        const val id = "XxXYP"
        const val imageinfo = "XxXKK"
        const val infos = "XxX4y"
        const val ignores = "XxXigs"
        const val isplaying = "XxXqe"
        const val isregex = "XxXqo"
        const val key = "XxXMG"
        const val level = "XxX1n"
        const val linkinfo = "XxXoW"
        const val links = "XxXmu"
        const val mediainfo = "XxXKv"
        const val meminfo = "XxXVd"
        const val mime = "XxXG2"
        const val notcopying = "XxXMA"
        const val notdeleting = "XxXxo"
        const val orientation = "XxX0m"
        const val overwriting = "XxXaA"
        const val pass = "XxXes"
        const val path = "XxXQm"
        const val paths = "XxXQms"
        const val player = "XxXjE"
        const val poster = "XxXLq"
        const val recorder = "XxX4q"
        const val result = "XxXJL"
        const val rpath = "XxXhk"
        const val rpaths = "XxXPB"
        const val searchIgnorecase = "XxXVc"
        const val serial = "XxXG9"
        const val size = "XxXpl"
        const val src = "XxXPm"
        const val stacktrace = "XxXBq"
        const val status = "XxXGT"
        const val style = "XxXEa"
        const val tag = "XxX2b"
        const val template = "XxX1I"
        const val text = "XxXOd"
        const val time = "XxXNn"
        const val timestamp = "XxXOS"
        const val tnpath = "XxXoz"
        const val total = "XxXcL"
        const val type = "XxXCl"
        const val url = "XxX9D"
        const val version = "XxXL9"
        const val warns = "XxX2P"
        const val width = "XxXUQ"
        const val supportwebp = "XxXhgm"
        const val xrefs = "XxXxrs"
    }

    object LinkInfoKey {
        const val BODY = "B"
        const val HEAD = "H"
        const val name = "n"
        const val rel = "r"
        const val status = "s"
        const val tag = "t"
        const val targetpath = "p"
        const val value = "v"
    }

    object LinkInfoStatus {
        const val EXISTS = "E"
        const val INVALID = "I"
        const val NOTEXISTS = "N"
    }

    object LinkVerifierCmd {
        const val INVALID = 0
        const val LINKINFO = 2
        const val LINKINFOS = 1
    }

    object MediaInfoCmd {
        const val INFOS = 1
        const val INVALID = 0
        const val THUMBNAILS = 2
    }

    object MetainfoKey {
        const val CreatedBy = "C"
        const val FeatureCalc = "Cc"
        const val Features = "F"
        const val ModifiedBy = "M"
        const val Patches = "P"
    }

    object NAME {
        const val browsingContextmenu = "XxXbE"
        const val contextmenu = "XxXJW"
        const val contextmenuAssists = "XxX3a"
        const val contextmenuChar = "XxXHD"
        const val contextmenuClasses = "XxXIV"
        const val contextmenuGallery = "XxXU0"
        const val contextmenuHighlight = "XxXKR"
        const val contextmenuImage = "XxXPz"
        const val contextmenuPage = "XxXYA"
        const val contextmenuPara = "XxXW1"
        const val contextmenuSelect = "XxX9x"
        const val contextmenuTemplate = "XxXrA"
        const val contextmenuWidget = "XxX3q"
        const val submenuChar = "XxXHE"
        const val submenuFont = "XxXIG"
        const val submenuList = "XxXVU"
        const val submenuMedia = "XxXAB"
        const val submenuOthers = "XxXS4"
        const val submenuPara = "XxXiJ"
    }

    object ObsoletedEventKey {
        const val AlarmOff = "disabled"
        const val Filepath = "filepath"
        const val Fragment = "fragment"
    }

    object ObsoletedEventRepeat {
        const val EveryDay = "Every day"
        const val EveryMonth = "Every month"
        const val EveryWeek = "Every week"
        const val EveryWeekday = "Every weekday"
        const val EveryWorkday = "Every workday"
        const val EveryYear = "Every year"
        const val Once = "Once"
    }

    object PATH {
        const val External = "External"
        const val External_ = "External/"
        const val Home = "Home"
        const val HomeIndexHtml = "Home/index.html"
        const val Home_ = "Home/"
        const val Internal = "Internal"
        const val Internal_ = "Internal/"
        const val _External_ = "/External/"
        const val _Home = "/Home"
        const val _HomeIncoming = "/Home/incoming"
        const val _HomeIndexHtml = "/Home/index.html"
        const val _Home_ = "/Home/"
        const val _InternalIncoming = "/Internal/incoming"
        const val _Internal_ = "/Internal/"
        const val _assetsAudioSample = "/assets/manual/samples/audios/audio-33.m4a"
        const val _assetsCSSEditorCss = "/assets/css/csseditor.css"
        const val _assetsCSSEditorHtml = "/assets/templates/res/csseditor.html"
        const val _assetsCSSEditorJs = "/assets/js/c.js"
        const val _assetsGameMindsCss = "/assets/css/gameminds.css"
        const val _assetsGameMindsHtml = "/assets/templates/res/gameminds.html"
        const val _assetsGameMindsJs = "/assets/js/g.js"
        const val _assetsGameMinesCss = "/assets/css/gamemines.css"
        const val _assetsGameMinesHtml = "/assets/templates/res/gamemines.html"
        const val _assetsGameMinesJs = "/assets/js/g.js"
        const val _assetsGameSudokuCss = "/assets/css/gamesudoku.css"
        const val _assetsGameSudokuHtml = "/assets/templates/res/gamesudoku.html"
        const val _assetsGameSudokuJs = "/assets/js/g.js"
        const val _assetsImagesLandscapeSample = "/assets/images/samples/backlight.jpg"
        const val _assetsImagesPortraitSample = "/assets/images/samples-oilify/pink.jpg"
        const val _assetsImagesSamplesAngels = "/assets/images/samples/angels.jpg"
        const val _assetsImagesSamplesLamp = "/assets/images/samples/lamp.jpg"
        const val _assetsImagesSig01Png = "/assets/images/res/sig01.png"
        const val _assetsReleaseNotesHtml = "/assets/manual/en/release-notes/release-notes.html"
        const val _assetsTemplates404Html = "/assets/templates/res/404.html"
        const val _assetsTemplates500Html = "/assets/templates/res/500.html"
        const val _assetsTemplatesFaviconIco = "/assets/templates/res/favicon.ico"
        const val _assetsTemplatesIndexHtml = "/assets/templates/res/index.html"
        const val _assetsVideoSample = "/assets/manual/samples/videos/BasicEditing.mp4"
        const val _internal = "/Internal"
        const val _pdfPoster = "/assets/templates/res/pdf.png"
        const val _videoPoster = "/assets/images/res/cloud1024c.png"
        const val _pdfSample = "/assets/manual/samples/pdf/release-notes.pdf"
        const val assetsTemplatesFaviconIco = "assets/templates/res/favicon.ico"
        const val assetsTemplatesIndexHtml = "assets/templates/res/index.html"
        const val HomeBlog = "Home/blog"
        const val HomeDrafts = "Home/drafts"
        const val Inbox_ = "Inbox/"
        const val PrivateBlog = "Private/blog"
        const val PrivateDrafts = "Private/drafts"
        const val Recover = "Recover"
        const val Recover_ = "Recover/"
        const val Restore = "Restore"
        const val Restore_ = "Restore/"
        const val _assets = "/assets"
        const val _assetsImageCss = "/assets/css/image.css"
        const val _assets_ = "/assets/"
        const val assets = "assets"
        const val assetsCSSEditorCss = "assets/css/csseditor.css"
        const val assetsCSSEditorHtml = "assets/templates/res/csseditor.html"
        const val assetsCSSEditorJs = "assets/js/c.js"
        const val assetsClientCss = "assets/css/client-v1.css"
        const val assetsClient_ = "assets/css/client-"
        const val assetsConfig = "assets/config"
        const val assetsConfig_ = "assets/config/"
        const val assetsCoreCss = "assets/css/core-v1.css"
        const val assetsCss_ = "assets/css/"
        const val assetsFonts_ = "assets/fonts/"
        const val assetsGameMindsCss = "assets/css/gameminds.css"
        const val assetsGameMindsHtml = "assets/templates/res/gameminds.html"
        const val assetsGameMindsJs = "assets/js/g.js"
        const val assetsGameMinesCss = "assets/css/gamemines.css"
        const val assetsGameMinesHtml = "assets/templates/res/gamemines.html"
        const val assetsGameMinesJs = "assets/js/g.js"
        const val assetsGameSudokuCss = "assets/css/gamesudoku.css"
        const val assetsGameSudokuHtml = "assets/templates/res/gamesudoku.html"
        const val assetsGameSudokuJs = "assets/js/g.js"
        const val assetsHostCss = "assets/css/host.css"
        const val assetsImageCss = "assets/css/image.css"
        const val assetsImages_ = "assets/images/"
        const val assetsJs_ = "assets/js/"
        const val assetsMainJs = "assets/js/m.js"
        const val assetsNativeJs = "assets/js/n.js"
        const val assetsResourcesJs = "assets/js/r.js"
        const val assetsTemplates404Html = "assets/templates/res/404.html"
        const val assetsTemplates500Html = "assets/templates/res/500.html"
        const val assetsTemplatesRes_ = "assets/templates/res/"
        const val assets_ = "assets/"
        const val blog = "blog"
        const val drafts = "drafts"
    }

    object Param {
        const val create = "create"
        const val edit = "edit"
        const val fragment = "fragment"
        const val ignorecase = "ignorecase"
        const val isregex = "isregex"
        const val mime = "mime"
        const val msg = "msg"
        const val path = "path"
        const val poster = "poster"
        const val redirect = "redirect"
        const val save = "save"
        const val searchtext = "searchtext"
        const val seek = "seek"
        const val session = "s"
        const val t = "t"
        const val view = "view"
    }

    object Placeholder {
        const val xpAnnotation = "annotation"
        const val xpAnnotationImage = "a-image"
        const val xpAnnotationPhoto = "a-photo"
        const val xpAnnotationPhotoNoCaption = "a-photo0"
        const val xpAudio = "audio"
        const val xpAudio2 = "audio2"
        const val xpAudioinfo = "audioinfo"
        const val xpCalendar = "calendar"
        const val xpCanvas = "canvas"
        const val xpCanvasImage = "canvas-image"
        const val xpCanvasPhoto = "canvas-photo"
        const val xpDate = "date"
        const val xpDateAuto = "date-auto"
        const val xpDateShort = "date-short"
        const val xpDatetime = "datetime"
        const val xpDatetimeShort = "datetime-short"
        const val xpDraggable = "draggable"
        const val xpImage = "image"
        const val xpImageNoCaption = "image0"
        const val xpPhoto = "photo"
        const val xpShortDate = "short-date"
        const val xpShortDatetime = "short-datetime"
        const val xpShortTime = "short-time"
        const val xpTime = "time"
        const val xpTimeShort = "time-short"
        const val xpVideo = "video"
        const val xpVideoinfo = "videoinfo"
    }

    object RESULT {
        const val CANCELED = 0
        const val FAILED = 1
        const val OK = -1
    }

    object ROLE {
        const val Generated = "generated"
    }

    object RecentsCmd {
        const val BACK = 4
        const val CLEAN = 2
        const val CLEAR = 1
        const val FORWARD = 6
        const val INFO = 3
        const val INVALID = 0
        const val PEEK = 5
        const val SORTED = 7
    }

    object SessionKey {
        const val loggedin = "loggedin"
        const val preferences = "perferences"
        const val recents = "recents"
        const val recentsByTime = "recentsByTime"
        const val videos = "videos"
        const val version = "version"
    }

    object SessionParam {
        const val XROOT = "X"
        const val X_ROOT = "x-root"
        const val poster = "p"
        const val type = "t"
        const val x = "x"
        const val y = "y"
        const val z = "z"
    }

    object SessionPreferencesKey {
        const val gameMindsCells = "GMDC"
        const val gameMindsDigits = "GMDD"
        const val gameMindsSave = "GMDS"
        const val gameSudokuDifficulty = "GSDD"
        const val gameSudokuAssists = "GSDA"
        const val gameSudokuSave = "GSDS"
        const val gameSudokuAutofill = "GSDF"
        const val gameMinesMapSize = "GMIM"
        const val gameMinesDifficulty = "GMID"
        const val gameMinesSave = "GMIS"
        const val symbolFamily = "SYBF"
        const val symbolRecents = "SYBR"
        const val showDoneEvents = "SDEV"
        const val templatesRecents = "TPLR"
        const val trashAutoCleanupTimestamp = "TACT"
        const val dirHistory = "DIRH"
        const val imageDefaultOutputSize = "IDOS"
        const val indentTab = "INDT"
        const val sidepanelWidth = "SPWD"
        const val filePositions = "FPOS"
        const val imageDefaultOutputQuality = "IDOQ"
        const val imageDefaultOutputFormat = "IDOF"
        const val photoDefaultOutputFormat = "PDOF"
        const val photoDefaultOutputSize = "PDOS"
        const val photoDefaultOutputQuality = "PDOQ"
    }

    object SettingsDefs {
        const val annotationColor = "#9400d3"
        const val buttonSize = 40
        const val dateFormat = "mm/dd/yyyy"
        const val dpi = 160
        const val fixedFontName = "UbuntuMono"
        const val fixedFontStyle = "Regular"
        const val fontSize = 16
        const val headingColor = "#1e90ff"
        const val highlightColor = "rgba(255,255,128,0.75)"
        const val imageDimension = 1024
        const val linkColor = "#0000ee"
        const val symbolFamily = "FontAwesome"
        const val timeFormat = "hh:mm"
        const val uiFontName = "Ruda"
        const val uiFontStyle = "Regular"
        const val winHeight = 640
        const val winWidth = 360
    }

    object SettingsKey {
        const val annotationColor = "annotationColor"
        const val bgImgSamples = "bgImgSamples"
        const val builtinCharStyles = "builtinCharStyles"
        const val builtinParaStyles = "builtinParaStyles"
        const val buttonSize = "buttonSize"
        const val charStyles = "charStyles"
        const val current = "current"
        const val dateFormat = "dateFormat"
        const val defaults = "defaults"
        const val dialogBGColor = "dialogBGColor"
        const val fixedFontName = "fixedFontName"
        const val fixedFontSize = "fixedFontSize"
        const val fixedFontStyle = "fixedFontStyle"
        const val fontCategories = "fontCategories"
        const val fontFamilies = "fontFamilies"
        const val headingColor = "boldColor"
        const val highlightColor = "highlightColor"
        const val highlightStyles = "highlightStyles"
        const val htmlTemplates = "htmlTemplates"
        const val imageDimension = "imageDimension"
        const val linkColor = "linkColor"
        const val paraStyles = "paraStyles"
        const val symbolFamilies = "symbolFamilies"
        const val timeFormat = "timeFormat"
        const val timeZone = "timeZone"
        const val uiFontName = "uiFontName"
        const val uiFontSize = "uiFontSize"
        const val uiFontStyle = "uiFontStyle"
    }

    object SidepanelInfoKey {
        const val sidebarHeight = "XxXEE"
    }

    object StyleKey {
        const val group = "group"
        const val label = "label"
        const val name = "name"
    }

    object THEME {
        const val splitPanelBG = "#a2cd5a"
    }

    object TemplateAction {
        const val annotation = "a"
        const val bg = "bg"
        const val blog = "b"
        const val classes = "classes"
        const val cutMarked = "x"
        const val cutNotMarked = "X"
        const val cycle = "c"
        const val cycle1 = "C"
        const val delete = "d"
        const val drag = "g"
        const val hide = "hide"
        const val move = "m"
        const val playall = "playall"
        const val postAlarm = "alarm"
        const val radio = "r"
        const val save = "save"
        const val show = "show"
        const val style = "s"
        const val template = "t"
        const val toggle = "T"
        const val widget = "widget"
    }

    object TemplateCat {
        const val Audio = "Audio"
        const val Blog = "Blog"
        const val Canvas = "Canvas"
        const val Home = "Home"
        const val Photo = "Photo"
    }

    object TemplateMenuKey {
        const val action = "a"
        const val background = "b"
        const val backgroundImage = "i"
        const val backgroundSize = "S"
        const val classes = "c"
        const val cmd = "C"
        const val label = "l"
        const val lcCat = "t"
        const val menu = "m"
        const val name = "n"
        const val path = "p"
        const val symbol = "s"
    }

    object TemplateName {
        const val audioV2 = "audio-v2"
        const val blogV2 = "blog-v2"
        const val homeSimpler = "home-simpler"
        const val mediaSticker = "media-sticker"
        const val mediaWall = "media-wall"
        const val photoSticker1 = "photo-sticker1"
        const val photoWall = "photo-wall"
    }

    object VERSIONS {
        const val ClientCssVersion = "1"
        const val TemplateVersion = "2"
    }

    object Widget {
        const val xAnnotation = "x-annotation"
        const val xFlexbox = "x-flexbox"
        const val xPictureFrame = "x-pictureframe"
        const val xPictureFrameContent = "x-wpf-content"
        const val xSidebarTab = "x-sidebartab"
        const val xSidepanel = "x-sidepanel"
        const val xSlideshow = "x-slideshow"
        const val xSplitpanel = "x-splitpanel"
        const val xSticker = "x-sticker"
    }

    object XAction {
        const val view = "view"
    }

    object XInfoKey {
        const val seek = "seek"
    }

    object XrefKey {
        const val LINKS = "l"
        const val POSITION = "p"
    }

    object XrefKind {
        const val ANCHOR = 2
        const val AUDIO = 8
        const val HREF = 256
        const val IMAGE = 4
        const val OTHER = 128
        const val SRC = 512
        const val SRCSET = 1024
        const val STYLESHEET = 1
        const val VIDEO = 16
    }

    object _FirstrunJSONKey {
        const val buildVersion = "B"
    }

    object _TemplatesJSONKey {
        const val aliases = "aliases"
        const val category = "category"
        const val classes = "classes"
        const val desc = "desc"
        const val emoji = "emoji"
        const val filepath = "filepath"
        const val label = "label"
        const val name = "name"
        const val template = "template"
        const val templates = "templates"
    }
}

object ServerKey {
    const val data = "XxXm0"
    const val headers = "XxXwd"
    const val ipc = "XxXr7"
    const val method = "XxXef"
    const val referrer = "XxXYI"
    const val serial = "XxXds"
    const val statusCode = "XxXgb"
    const val url = "XxXGV"
}
