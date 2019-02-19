def text = new File("/mnt/0388AD112E82245F/NETGRIF/NAE/app/src/test/resources/insurance_portal_demo_test.xml").text

Counter c = new Counter()


new File("/mnt/0388AD112E82245F/NETGRIF/NAE/app/src/test/resources/insurance_portal_demo_test.xml").text = text.replaceAll("<action") {
    "<action id=\"${c.getNextId()}\""
}

class Counter {
    def id = 0

    def getNextId() {
        this.id = id + 1
        return id
    }
}