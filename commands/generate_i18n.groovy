def FILE_PATH = "../src/test/resources/mapping_test.xml"

def I18N_REGEX = /name="[a-zA-Z_]+"/

def file = new File(FILE_PATH)
def names = file.text.findAll(I18N_REGEX)

names.each {
    println it
}