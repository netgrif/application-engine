import groovy.xml.MarkupBuilder

def FILE_PATH
def LOCALE

println "Model path:"
FILE_PATH = System.in.newReader().readLine()
println "Locale:"
LOCALE = System.in.newReader().readLine()

def I18N_REGEX = /name="[a-zA-Z_]+"/

def file = new File(FILE_PATH)
def names = file.text.findAll(I18N_REGEX)
def writer = new StringWriter()
def xml = new MarkupBuilder(writer)

xml.i18n(locale: LOCALE) {
    names.each { name ->
        xml.i18nString("name": "${name.substring(6, name.length() - 1)}", "")
    }
}

new File(file.name).text = writer.toString()

println "Locale generated"