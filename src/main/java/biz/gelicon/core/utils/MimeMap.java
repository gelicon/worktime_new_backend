package biz.gelicon.core.utils;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MimeMap {

    private static final Map<String, String> map = new HashMap<>();

    static {
        addContentType("xml", "text/xml");
        addContentType("abs", "audio/x-mpeg");
        addContentType("ai", "application/postscript");
        addContentType("aif", "audio/x-aiff");
        addContentType("aifc", "audio/x-aiff");
        addContentType("aiff", "audio/x-aiff");
        addContentType("aim", "application/x-aim");
        addContentType("art", "image/x-jg");
        addContentType("asf", "video/x-ms-asf");
        addContentType("asx", "video/x-ms-asf");
        addContentType("au", "audio/basic");
        addContentType("avi", "video/x-msvideo");
        addContentType("avx", "video/x-rad-screenplay");
        addContentType("bcpio", "application/x-bcpio");
        addContentType("bin", "application/octet-stream");
        addContentType("bmp", "image/bmp");
        addContentType("body", "text/html");
        addContentType("cdf", "application/x-cdf");
        addContentType("cer", "application/x-x509-ca-cert");
        addContentType("class", "application/java");
        addContentType("cpio", "application/x-cpio");
        addContentType("csh", "application/x-csh");
        addContentType("css", "text/css");
        addContentType("dib", "image/bmp");
        addContentType("doc", "application/msword");
        addContentType("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        addContentType("dtd", "text/plain");
        addContentType("dv", "video/x-dv");
        addContentType("dvi", "application/x-dvi");
        addContentType("eps", "application/postscript");
        addContentType("etx", "text/x-setext");
        addContentType("exe", "application/octet-stream");
        addContentType("gif", "image/gif");
        addContentType("gtar", "application/x-gtar");
        addContentType("gz", "application/x-gzip");
        addContentType("hdf", "application/x-hdf");
        addContentType("hqx", "application/mac-binhex40");
        addContentType("htm", "text/html");
        addContentType("html", "text/html");
        addContentType("hqx", "application/mac-binhex40");
        addContentType("ief", "image/ief");
        addContentType("jad", "text/vnd.sun.j2me.app-descriptor");
        addContentType("jar", "application/java-archive");
        addContentType("java", "text/plain");
        addContentType("jnlp", "application/x-java-jnlp-file");
        addContentType("jpe", "image/jpeg");
        addContentType("jpeg", "image/jpeg");
        addContentType("jpg", "image/jpeg");
        addContentType("js", "text/javascript");
        addContentType("kar", "audio/x-midi");
        addContentType("latex", "application/x-latex");
        addContentType("m3u", "audio/x-mpegurl");
        addContentType("mac", "image/x-macpaint");
        addContentType("man", "application/x-troff-man");
        addContentType("me", "application/x-troff-me");
        addContentType("mid", "audio/x-midi");
        addContentType("midi", "audio/x-midi");
        addContentType("mif", "application/x-mif");
        addContentType("mov", "video/quicktime");
        addContentType("movie", "video/x-sgi-movie");
        addContentType("mp1", "audio/x-mpeg");
        addContentType("mp2", "audio/x-mpeg");
        addContentType("mp3", "audio/x-mpeg");
        addContentType("mpa", "audio/x-mpeg");
        addContentType("mpe", "video/mpeg");
        addContentType("mpeg", "video/mpeg");
        addContentType("mpega", "audio/x-mpeg");
        addContentType("mpg", "video/mpeg");
        addContentType("mpv2", "video/mpeg2");
        addContentType("ms", "application/x-wais-source");
        addContentType("nc", "application/x-netcdf");
        addContentType("oda", "application/oda");
        addContentType("pbm", "image/x-portable-bitmap");
        addContentType("pct", "image/pict");
        addContentType("pdf", "application/pdf");
        addContentType("pgm", "image/x-portable-graymap");
        addContentType("pic", "image/pict");
        addContentType("pict", "image/pict");
        addContentType("pls", "audio/x-scpls");
        addContentType("png", "image/png");
        addContentType("pnm", "image/x-portable-anymap");
        addContentType("pnt", "image/x-macpaint");
        addContentType("ppm", "image/x-portable-pixmap");
        addContentType("ppt", "application/vnd.ms-powerpoint");
        addContentType("ps", "application/postscript");
        addContentType("psd", "image/x-photoshop");
        addContentType("qt", "video/quicktime");
        addContentType("qti", "image/x-quicktime");
        addContentType("qtif", "image/x-quicktime");
        addContentType("ras", "image/x-cmu-raster");
        addContentType("rgb", "image/x-rgb");
        addContentType("rm", "application/vnd.rn-realmedia");
        addContentType("roff", "application/x-troff");
        addContentType("rtf", "application/rtf");
        addContentType("rtx", "text/richtext");
        addContentType("sh", "application/x-sh");
        addContentType("shar", "application/x-shar");
        addContentType("smf", "audio/x-midi");
        addContentType("snd", "audio/basic");
        addContentType("src", "application/x-wais-source");
        addContentType("sv4cpio", "application/x-sv4cpio");
        addContentType("sv4crc", "application/x-sv4crc");
        addContentType("swf", "application/x-shockwave-flash");
        addContentType("svg", "image/svg+xml");
        addContentType("svgz", "image/svg+xml");
        addContentType("t", "application/x-troff");
        addContentType("tar", "application/x-tar");
        addContentType("tcl", "application/x-tcl");
        addContentType("tex", "application/x-tex");
        addContentType("texi", "application/x-texinfo");
        addContentType("texinfo", "application/x-texinfo");
        addContentType("tif", "image/tiff");
        addContentType("tiff", "image/tiff");
        addContentType("tr", "application/x-troff");
        addContentType("tsv", "text/tab-separated-values");
        addContentType("txt", "text/plain");
        addContentType("ulw", "audio/basic");
        addContentType("ustar", "application/x-ustar");
        addContentType("xbm", "image/x-xbitmap");
        addContentType("xpm", "image/x-xpixmap");
        addContentType("xls", "application/vnd.ms-excel");
        addContentType("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        addContentType("xwd", "image/x-xwindowdump");
        addContentType("wav", "audio/x-wav");
        // <!-- Wireless Bitmap -->
        addContentType("wbmp", "image/vnd.wap.wbmp");
        // <!-- WML Source -->
        addContentType("wml", "text/vnd.wap.wml");
        // <!-- Compiled WML -->
        addContentType("wmlc", "application/vnd.wap.wmlc");
        // <!-- WML Script Source -->
        addContentType("wmls", "text/vnd.wap.wmls");
        // <!-- Compiled WML Script -->
        addContentType("wmlscriptc", "application/vnd.wap.wmlscriptc");
        addContentType("wrl", "x-world/x-vrml");
        addContentType("Z", "application/x-compress");
        addContentType("z", "application/x-compress");
        addContentType("zip", "application/zip");
        addContentType("mp4", "video/mp4");
        addContentType("ogg", "video/ogg");

    }

    public static void addContentType(String extn, String type) {
        map.put(extn.toLowerCase(), type.toLowerCase());
    }

    public static void addContentType(Map<String, String> source) {
        for (Entry<String, String> e : source.entrySet()) {
            addContentType(e.getKey(), e.getValue());
        }
    }

    public static Set<String> getExtensions() {
        return map.keySet();
    }

    public static String getContentType(String extn) {
        String mime = map.get(extn.toLowerCase());
        return mime != null ? mime : map.get("bin");
    }

    public static void removeContentType(String extn) {
        map.remove(extn.toLowerCase());
    }

    /**
     * Get extension of file, without fragment id
     */
    public static String getExtension(String fileName) {
        // play it safe and get rid of any fragment id
        // that might be there
        int length = fileName.length();

        int newEnd = fileName.lastIndexOf('#');
        if (newEnd == -1)
            newEnd = length;
        // Instead of creating a new string.
        // if (i != -1) {
        // fileName = fileName.substring(0, i);
        // }
        int i = fileName.lastIndexOf('.', newEnd);
        if (i != -1) {
            return fileName.substring(i + 1, newEnd);
        } else {
            // no extension, no content type
            return null;
        }
    }

    public static String getContentTypeFor(String fileName) {
        String extn = getExtension(fileName);
        return extn != null ? getContentType(extn) : null;
    }

    public static String getContentTypeFor(String fileName, String def) {
        String ret = getContentTypeFor(fileName);
        return ret != null ? ret : def;

    }

}
