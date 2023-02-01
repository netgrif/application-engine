# PDF Generator

PDF Generator is a tool implemented in Netgrif Application Engine for generating PDF documents from dynamic forms.

## Tool services

The PDF generator tool consists of three main Java Services:

- **PdfDataHelper** - this java bean gets the task data in form of **DataFields** and converts these objects to list
  of **PdfFields**, that represents a DataField in the final PdfFile. It gets the preset parameters of the final PDF
  document, that were defined by the user. It calculates the required height, width and position of each PdfField.
- **PdfDrawer** - gets the list of PdfFields and their properties and attributes and based on these attributes renders
  these field to the final **PdfDocument** using field render classes. It draws check boxes, radio buttons, field
  squares and rectangles, titles, data group titles, field titles and values for fields.
- **PdfGenerator** - the main class for the PDF generator, that initializes final file, reads template if needed, sends
  data to PdfDataHelper and PdfDrawer, iterates through the PdfField list and decides, which field will be rendered in
  which way.

## Configuration

When you are defining own action in Action API for generating PDF using custom configuration, you need to set
configuration attributes then set up PdfGenerator and then generate the pdf.

The properties for the PdfGenerator can be set using application properties files and Java code too. The **PdfResource**
and its superclass, the **PdfProperties** are Spring Components and their attributes are configuration properties. These
properties can be set either in application.properties file with **nae.pdf.resources.** for PdfResource and
**nae.pdf.properties** for PdfProperties prefix or directly in actions using Java/Groovy code by firstly getting the Java
Bean of these components and afterwards using getters and setters on attributes.

The following properties can be set with nae.pdf.properties.* prefix or using PdfProperties class with their default
values:

```java
public class PdfProperties {
    protected int unit = 75;
    protected PDRectangle pageSize = PDRectangle.A4;
    protected int pageWidth = 600;
    protected int pageHeight = 850;
    protected int lineHeight = 20;
    protected int marginTitle = (int) (0.5 * unit);
    protected int marginTop = unit;
    protected int marginBottom = unit;
    protected int marginLeft = (int) (0.5 * unit);
    protected int marginRight = (int) (0.5 * unit);
    protected int padding = 4;
    protected int boxPadding = 2;
    protected int baseX = marginLeft;
    protected int baseY;
    protected int pageDrawableWidth = pageWidth - marginLeft - marginRight;
    protected int fontTitleSize = 13;
    protected int fontGroupSize = 13;
    protected int fontLabelSize = 10;
    protected int fontValueSize = 10;
    protected int formGridCols = 4;
    protected int formGridRows = 30;
    protected int formGridColWidth = (pageDrawableWidth / formGridCols);
    protected int formGridRowHeight = ((pageHeight - marginBottom - marginTop) / formGridRows);
    protected int rowGridFree = formGridCols;
    protected float strokeWidth = 0.5f;
    protected int boxSize = 10;
    protected float sizeMultiplier = 1.65f;
    protected int pageNumberPosition = (int) (0.5 * pageWidth);
    protected String documentTitle = "";
    protected boolean textFieldStroke = true;
    protected boolean booleanFieldStroke = false;
    protected PdfDateFormat dateFormat = PdfDateFormat.SLOVAK1;
    protected PdfDateFormat dateTimeFormat = PdfDateFormat.SLOVAK1_DATETIME;
    protected Locale numberFormat = new Locale("sk", "SK");
    protected Locale textLocale = new Locale("sk", "SK");
    protected PdfBooleanFormat booleanFormat = PdfBooleanFormat.DOUBLE_BOX_WITH_TEXT_SK;
    protected PdfPageNumberFormat pageNumberFormat = PdfPageNumberFormat.SLASH;
}
```

The following properties can be set with **nae.pdf.resources.** prefix or using PdfResource class:

```java
public class PdfResource {
    private PDType0Font labelFont;
    private PDType0Font titleFont;
    private PDType0Font valueFont;
    private PDFormXObject checkboxChecked;
    private PDFormXObject checkboxUnchecked;
    private PDFormXObject radioChecked;
    private PDFormXObject radioUnchecked;
    private PDFormXObject booleanChecked;
    private PDFormXObject booleanUnchecked;
    private Resource fontTitleResource;
    private Resource fontLabelResource;
    private Resource fontValueResource;
    private String outputFolder;
    private String outputDefaultName;
    private Resource outputResource;
    private Resource templateResource;
    private Resource checkBoxCheckedResource;
    private Resource checkBoxUnCheckedResource;
    private Resource radioCheckedResource;
    private Resource radioUnCheckedResource;
    private Resource booleanCheckedResource;
    private Resource booleanUncheckedResource;
}
```

As the `PdfResorce` class extends PdfProperties class, when setting up properties through beans, it is enough to get only
PdfResource bean:

| Example in application.properties                                                                     | Example of getting bean and setting attribute in actions|
|-------------------------------------------------------------------------------------------------------|---------------------------------------------------------|
| `properties nae.pdf.properties.pageWidth=650`                                                         | `java PdfResource pdfResource = ApplicationContextProvider.getBean(PdfResource.class) as PdfResource` `java pdfResource.setMarginLeft(75)` |
| `properties nae.pdf.resources.templateResource=file:src/main/resources/pdfGenerator/template_pdf.pdf` | `java PdfResource pdfResource = ApplicationContextProvider.getBean(PdfResource.class) as PdfResource` `java pdfResource.setMarginLeft(75)`|

## Setup

PDF generator services are defined as Java Beans, so they will be initialized at start up of the Spring Boot
application. The default values for PdfProperties are set in the class itself and the default values for PdfResources
are defined in application properties. Likely enough, your application is already containing the default configuration
values for PdfResources in your application.properties and required file resources are placed under
src/main/resources/pdfGenerator folder.

However, as each PDF can be different, it is enough to set up the generator straight before the generating. In Actions
API you can find 4 predefined functions for generating PDF, so when you are using the Netgrif Application Engine, you
have some functions predefined, but if you would like to make some custom configuration, it is enough to change
properties in application.properties, implement action for changing the properties and/or put additional font or image
files to src/main/resources/pdfGenerator path.

The example of custom action:

```groovy
class ActionDelegate {

    void generatePdfWithTemplate(String transitionId, String fileFieldId) {
        PdfResource pdfResource = ApplicationContextProvider.getBean(PdfResource.class) as PdfResource
        String filename = pdfResource.getOutputDefaultName()
        String storagePath = pdfResource.getOutputFolder() + File.separator + useCase.stringId + "-" + fileFieldId + "-" + pdfResource.getOutputDefaultName()

        pdfResource.setOutputResource(new ClassPathResource(storagePath))
        pdfResource.setMarginTitle(100)
        pdfResource.setMarginLeft(75)
        pdfResource.setMarginRight(75)
        pdfResource.updateProperties()
        pdfGenerator.setupPdfGenerator(pdfResource)
        pdfGenerator.generatePdf(useCase, transitionId, pdfResource)
        change useCase.getField(fileFieldId) value {
            new FileFieldValue(filename, storagePath)
        }
    }

}
```

## Usage

A custom function, as the predefined ones, must be implemented as part of Action API. Then this function can be called
in action of a workflow Petriflow definition. Alternatively, the whole function can be defined, as an action of a
workflow Petriflow definition.

Let’s have a look for the example above. So first, you need to get bean of the PdfResource, that already contains some
default configuration or custom configuration from application.properties. Then you can define a path and name for final
PDF file. This can be a simple location in the system or a FileFieldValue to additionally put the file into file field.

After that if you can change the default configuration using the PdfResource. Then you have to set up the PdfGenerator
itself using the properties. After that you can generate the PDF using the PDF generator. When generating the PDF, you
will have to provide the Task or some information to the task itself, its form will be exported/generated to PDF. The
PdfGenerator.generatePdf(Case, String, PdfResource) function takes three arguments:

- `Case useCase` - the case, which contains task, from which the PDF will be generated.
- `String transitionId` - the ID of transition, that is instantiated as Task in useCase.
- `PdfResource pdfResource` - the configuration for PdfGenerator

Finally, based on the path or FileFieldValue in the first part of function, you can change a file fields value to the
final PDF file.

If you do not want to define your own implementation of PDF generation action, you still can use the default actions for
generating PDF, which are the followings:

```groovy
@NamedVariant
void generatePDF(String sourceTransitionId, String targetFileFieldId,
                 Case sourceCase = useCase, Case targetCase = useCase, String targetTransitionId = null,
                 String template = null, List<String> excludedFields = [], Locale locale = null,
                 ZoneId dateZoneId = ZoneId.systemDefault(), Integer sideMargin = 75, Integer titleMargin = 20) {
    if (!sourceTransitionId || !targetFileFieldId)
        throw new IllegalArgumentException("Source transition or target file field is null")
    targetTransitionId = targetTransitionId ?: sourceTransitionId
    generatePdf(sourceTransitionId, targetFileFieldId, sourceCase, targetCase, targetTransitionId,
            template, excludedFields, locale, dateZoneId, sideMargin, titleMargin)
}

void generatePDF(Transition sourceTransition, FileField targetFileField, Case sourceCase = useCase, Case targetCase = useCase,
                 Transition targetTransition = null, String template = null, List<String> excludedFields = [], Locale locale = null,
                 ZoneId dateZoneId = ZoneId.systemDefault(), Integer sideMargin = 75, Integer titleMargin = 0) {
    if (!sourceTransition || !targetFileField)
        throw new IllegalArgumentException("Source transition or target file field is null")
    targetTransition = targetTransition ?: sourceTransition
    generatePdf(sourceTransition.stringId, targetFileField.importId, sourceCase, targetCase, targetTransition.stringId,
            template, excludedFields, locale, dateZoneId, sideMargin, titleMargin)
}

void generatePdf(String transitionId, FileField fileField, List<String> excludedFields = []) {
    generatePdf(sourceTransitionId: transitionId, targetFileFieldId: fileField, excludedFields: excludedFields)
}

void generatePdf(String transitionId, String fileFieldId, List<String> excludedFields, Case fromCase = useCase, Case saveToCase = useCase) {
    generatePdf(sourceTransitionId: transitionId, targetFileFieldId: fileFieldId, excludedFields: excludedFields, sourceCase: fromCase, targetCase: useCase)
}

void generatePdfWithTemplate(String transitionId, String fileFieldId, String template, Case fromCase = useCase, Case saveToCase = useCase) {
    generatePdf(sourceTransitionId: transitionId, targetFileFieldId: fileFieldId, template: template, sourceCase: fromCase, targetCase: saveToCase)
}

void generatePdfWithLocale(String transitionId, String fileFieldId, Locale locale, Case fromCase = useCase, Case saveToCase = useCase) {
    generatePdf(sourceTransitionId: transitionId, targetFileFieldId: fileFieldId, locale: locale, sourceCase: fromCase, targetCase: saveToCase)
}
```

These actions can be simply called in your PetriNet action definitions.

## Supported layouts

PDF generator supports standard legacy layout, grid layout and flow layout. You have nothing to do with layout
configuration as the PDF generator resolves it automatically.
