
package nonprofitbookkeeping.ui.panels;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * Minimal JavaFX replacement for the Swing {@code PageViewer}. Allows the user
 * to open a PDF file, renders each page to an {@link Image}, and shows Prev /
 * Next navigation.  Uses <a href="https://pdfbox.apache.org/">PDFBox</a> for
 * rendering (already in many build setups).
 */
public class PageViewerFX extends BorderPane
{
	
	/** ImageView component to display the currently rendered PDF page. */
	private final ImageView imageView = new ImageView();
	/** List to store each page of the loaded PDF as a JavaFX {@link Image} object. Null until a PDF is loaded. */
	private List<Image> pages;
	/** AtomicInteger to keep track of the currently displayed page index (0-based). */
	private final AtomicInteger pageIndex = new AtomicInteger(0);
	
	/**
	 * Constructs a new {@code PageViewerFX}.
	 * Initializes the panel with an {@link ImageView} for displaying PDF pages,
	 * a {@link ScrollPane} for the image view, and a toolbar for navigation and opening files.
	 *
	 * @param owner The parent {@link Stage}, used as the owner for the {@link FileChooser} dialog.
	 */
	public PageViewerFX(Stage owner)
	{
		setPadding(new Insets(10));
		this.imageView.setPreserveRatio(true);
		ScrollPane scroller = new ScrollPane(this.imageView);
		scroller.setFitToWidth(true);
		scroller.setFitToHeight(true);
		setCenter(scroller);
		setTop(buildToolbar(owner));
	}
	
	/**
	 * Builds and returns a {@link ToolBar} for PDF navigation and file operations.
	 * The toolbar includes an "Open PDF" button, "Prev" and "Next" page navigation buttons,
	 * and a label to display the current page number and total pages (e.g., "Page 1/5").
	 * Button actions are defined for opening a PDF and navigating pages.
	 * Navigation buttons are disabled appropriately (e.g., "Prev" on the first page).
	 *
	 * @param owner The parent {@link Stage} to be used as the owner for the "Open PDF" {@link FileChooser} dialog.
	 * @return A configured {@link ToolBar} with PDF viewing controls.
	 */
	private ToolBar buildToolbar(Stage owner)
	{
		Button open = new Button("Open PDF");
		Button prev = new Button("◀ Prev");
		Button next = new Button("Next ▶");
		Label pageLbl = new Label("Page 0/0");
		
		open.setOnAction(e -> {
			FileChooser fc = new FileChooser();
			fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
			File f = fc.showOpenDialog(owner);
			if (f != null)
				loadPdf(f, pageLbl);
		});
		prev.setOnAction(e -> showPage(this.pageIndex.decrementAndGet(), pageLbl));
		next.setOnAction(e -> showPage(this.pageIndex.incrementAndGet(), pageLbl));
		prev.disableProperty().bind(pageLbl.textProperty().isEqualTo("Page 0/0"));
		next.disableProperty().bind(prev.disableProperty());
		return new ToolBar(open, new Separator(), prev, next, pageLbl);
	}
	
	/**
	 * Loads a PDF document from the specified {@link File}, renders each page into an {@link Image},
	 * and stores these images in the {@link #pages} list.
	 * After loading, it sets the current page index to 0 and displays the first page using {@link #showPage(int, Label)}.
	 * If any error occurs during loading or rendering (e.g., I/O error, PDF parsing error),
	 * an error alert is displayed to the user.
	 *
	 * @param f The PDF {@link File} to load. Must not be null.
	 * @param lbl The {@link Label} used to display page information (e.g., "Page X/Y"), which will be updated by {@code showPage}.
	 */
	private void loadPdf(File f, Label lbl)
	{
		
		try (PDDocument doc = PDDocument.load(f))
		{
			PDFRenderer renderer = new PDFRenderer(doc);
			this.pages = new java.util.ArrayList<>();
			
			for (int i = 0; i < doc.getNumberOfPages(); i++)
			{
				java.awt.image.BufferedImage bim = renderer.renderImageWithDPI(i, 110);
				Image fx = SwingFXUtils.toFXImage(bim, null);
				this.pages.add(fx);
			}
			
			this.pageIndex.set(0);
			showPage(0, lbl);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			new Alert(Alert.AlertType.ERROR, "Failed to open PDF: " + ex.getMessage())
				.showAndWait();
		}
		
	}
	
	/**
	 * Displays the PDF page at the specified index ({@code idx}) in the {@link #imageView}.
	 * The method ensures the index is within the valid range of available pages (0 to {@code pages.size() - 1}).
	 * It updates the {@link #pageIndex} to the (potentially corrected) index and sets the image
	 * in the {@code imageView}. The {@code imageView}'s fit width is adjusted, and the page
	 * information label ({@code lbl}) is updated to reflect the current page and total pages.
	 * If no pages are loaded ({@link #pages} is null or empty), this method does nothing.
	 *
	 * @param idx The 0-based index of the page to display.
	 * @param lbl The {@link Label} to update with the current page information (e.g., "Page X/Y").
	 */
	private void showPage(int idx, Label lbl)
	{
		if (this.pages == null || this.pages.isEmpty())
			return;
		idx = Math.max(0, Math.min(idx, this.pages.size() - 1));
		this.pageIndex.set(idx);
		this.imageView.setImage(this.pages.get(idx));
		this.imageView.setFitWidth(getWidth() - 40);
		lbl.setText("Page " + (idx + 1) + "/" + this.pages.size());
	}
	
}
