
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
	
	private final ImageView imageView = new ImageView();
	private List<Image> pages;
	private final AtomicInteger pageIndex = new AtomicInteger(0);
	
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
