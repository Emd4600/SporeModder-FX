/****************************************************************************
* Copyright (C) 2019 Eric Mor
*
* This file is part of SporeModder FX.
*
* SporeModder FX is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
****************************************************************************/
package sporemodder.view.editors;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import javax.imageio.ImageIO;

import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.effect.ImageInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import sporemodder.FileManager;
import sporemodder.UIManager;
import sporemodder.file.bitmaps.BitmapImage;
import sporemodder.file.dds.DDSTexture;
import sporemodder.file.raster.RasterTexture;
import sporemodder.file.rw4.RWHeader.RenderWareType;
import sporemodder.file.rw4.RenderWare;
import sporemodder.util.ProjectItem;
import sporemodder.view.UserInterface;

/**
 * An editor used for visualizing images and textures. It is called 'viewer' instead of 'editor' because images are not editable; they can't
 * be modified nor saved.
 */
public class ImageViewer implements ItemEditor {
	
	private static final List<String> SUPPORTED_TYPES = Arrays.asList("png", "jpg", "jpeg", "bmp", "dds", "rw4", "rast", "raster", "bitImage", "8bitImage", "32bitImage", "48bitImage");
	
	private static final double MIN_ZOOM = 0.25;
	private static final double MAX_ZOOM = 10;
	private static final double ZOOM_STEP = 0.25;

	public static class Factory implements EditorFactory {
		
		@Override
		public ItemEditor createInstance() {
			return new ImageViewer();
		}

		@Override
		public boolean isSupportedFile(ProjectItem item) {
			String extension = item.getSpecificExtension();
			
			if (!item.isFolder() && SUPPORTED_TYPES.contains(extension)) {
				if (extension.equals("rw4")) {
					try {
						return RenderWare.peekType(item.getFile()) == RenderWareType.TEXTURE;
					}
					catch (IOException e) {
						return false;
					}
				}
				
				return true;
			}
			
			return false;
		}
		
		@Override
		public Node getIcon(ProjectItem item) {
			return null;
		}
	}
	
	private ScrollPane mainNode;
	
	private String imageType;
	private File file;
	
	// DDS are read as BufferedImages, then converted to JavaFX images
	// During that process, they lose the color information on transparent pixels, so we keep this here
	// Not all types use this
	private BufferedImage bufferedImage;
	private Image originalImage;
	private ImageView imageView;
	private BorderPane imagePane;
	
	private Button exportAsDDS;
	private Button exportAsPNG;
	
	private Spinner<Double> zoomLvlSpinner;
	
	private CheckBox cbRedMask;
	private CheckBox cbGreenMask;
	private CheckBox cbBlueMask;
	private CheckBox cbAlphaMask;
	
	//private Pane inspectorUI;
	private Pane inspector;
	
	private ImageViewer() {
		super();
		
		initializeUI();
	}
	
	private void initializeUI() {
		mainNode = new ScrollPane();
		mainNode.setHbarPolicy(ScrollBarPolicy.ALWAYS);
		mainNode.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		
		imageView = new ImageView();
		
		imagePane = new BorderPane();
		mainNode.setContent(imagePane);
		
		mainNode.setFitToWidth(true);
		mainNode.setFitToHeight(true);
		mainNode.setPannable(true);
		imagePane.setCenter(imageView);
		imagePane.setOnScroll((event) -> {
			if (event.isAltDown()) {
				if (event.getDeltaY() > 0) {
					zoomLvlSpinner.increment();
				} else {
					zoomLvlSpinner.decrement();
				}
				// The ScrollPane will move otherwise
				event.consume();
			}
		});
		
		DoubleSpinnerValueFactory zoomFactory = new DoubleSpinnerValueFactory(MIN_ZOOM, MAX_ZOOM, 1, ZOOM_STEP);
		zoomFactory.setConverter(new StringConverter<Double>() {

			@Override
			public Double fromString(String value) {
				value = value.split("%")[0].trim();
				if (value.isEmpty()) {
					return 1.0;
				}
				else {
					return Double.parseDouble(value) / 100.0;
				}
			}

			@Override
			public String toString(Double value) {
				return Double.toString(value * 100) + "%";
			}
			
		});
		
		exportAsDDS = new Button("Export as DDS");
		exportAsDDS.setOnAction((event) -> {
			exportDDS();
		});
		
		exportAsPNG = new Button("Export as PNG");
		exportAsPNG.setOnAction((event) -> {
			exportPNG();
		});
		
		zoomLvlSpinner = new Spinner<Double>();
		zoomLvlSpinner.setPrefWidth(Double.MAX_VALUE);
		zoomLvlSpinner.setValueFactory(zoomFactory);
		zoomLvlSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
			imageView.setScaleX(newValue);
			imageView.setScaleY(newValue);
			
			imagePane.setMinWidth(imageView.getImage().getWidth() * newValue);
			imagePane.setMinHeight(imageView.getImage().getHeight() * newValue);
			
			mainNode.layout();
		});
		zoomLvlSpinner.setEditable(true);
		zoomLvlSpinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				zoomLvlSpinner.increment(0); // won't change value, but will commit editor
			}
		});
		
		cbRedMask = new CheckBox("Red");
		cbGreenMask = new CheckBox("Green");
		cbBlueMask = new CheckBox("Blue");
		cbAlphaMask = new CheckBox("Alpha");
		
		cbRedMask.setSelected(true);
		cbGreenMask.setSelected(true);
		cbBlueMask.setSelected(true);
		cbAlphaMask.setSelected(true);
		
		ChangeListener<? super Boolean> channelsListener = (obs, oldValue, newValue) -> {
			updateChannels();
		};
		
		cbRedMask.selectedProperty().addListener(channelsListener);
		cbGreenMask.selectedProperty().addListener(channelsListener);
		cbBlueMask.selectedProperty().addListener(channelsListener);
		cbAlphaMask.selectedProperty().addListener(channelsListener);
		
		inspector = new VBox(5);
		inspector.setPadding(new Insets(5, 0, 0, 0));
	}
	
	private void updateChannels() {
		
		// Restore original image
		imageView.setImage(originalImage);
		imageView.setEffect(null);

		if (cbRedMask.isSelected() && cbGreenMask.isSelected() 
				&& cbBlueMask.isSelected() && cbAlphaMask.isSelected()) {
			imageView.setEffect(null);
		}
		else {
			int red = cbRedMask.isSelected() ? 255 : 0;
			int green = cbGreenMask.isSelected() ? 255 : 0;
			int blue = cbBlueMask.isSelected() ? 255 : 0;
			
			if ((!cbAlphaMask.isSelected() && red + green + blue == 255) || (cbAlphaMask.isSelected() && red + green + blue == 0))
			{
				// Only one channel selected, show it as grayscale
				
				int width = (int) originalImage.getWidth();
				int height = (int) originalImage.getHeight();
				WritableImage newImage = new WritableImage(width, height);
				
				PixelWriter writer = newImage.getPixelWriter();
				
				BiFunction<Integer, Integer, Double> f;
				
				if (bufferedImage != null) {
					if (red != 0) 			f = (x, y) -> ((bufferedImage.getRGB(x, y) & 0x00FF0000) >> 16) / 255.0;
					else if (green != 0) 	f = (x, y) -> ((bufferedImage.getRGB(x, y) & 0x0000FF00) >> 8) / 255.0;
					else if (blue != 0) 	f = (x, y) -> ((bufferedImage.getRGB(x, y) & 0x000000FF) >> 0) / 255.0;
					else 					f = (x, y) -> ((bufferedImage.getRGB(x, y) & 0xFF000000) >> 24) / 255.0;
				}
				else {
					PixelReader reader = originalImage.getPixelReader();
					if (red != 0) f = (x, y) -> reader.getColor(x, y).getRed();
					else if (green != 0) f = (x, y) -> reader.getColor(x, y).getGreen();
					else if (blue != 0) f = (x, y) -> reader.getColor(x, y).getBlue();
					else f = (x, y) -> reader.getColor(x, y).getOpacity();
				}
				
				for (int x = 0; x < width; x++) {
					for (int y = 0; y < height; y++) {
						writer.setColor(x, y, Color.gray(f.apply(x, y)));
					}
				}
				
				imageView.setImage(newImage);
			}
			else {
				if (bufferedImage == null) {
					Color blendColor = Color.rgb(red, green, blue, 1.0);
					
					Blend blend = new Blend(BlendMode.MULTIPLY,
							new ImageInput(originalImage),
							new ColorInput(0, 0, originalImage.getWidth(), originalImage.getHeight(), blendColor));
					
					if (!cbAlphaMask.isSelected()) {
						blend = new Blend(BlendMode.ADD,
								blend,
								new ColorInput(0, 0, originalImage.getWidth(), originalImage.getHeight(), new Color(0, 0, 0, 1.0)));
					}
					
					imageView.setEffect(blend);
				}
				else {
					int width = (int) originalImage.getWidth();
					int height = (int) originalImage.getHeight();
					WritableImage newImage = new WritableImage(width, height);
					PixelWriter writer = newImage.getPixelWriter();
					
					int mask = (red << 16) | (green << 8) | blue;
					if (cbAlphaMask.isSelected()) mask |= 0xFF000000;
					
					for (int x = 0; x < width; x++) {
						for (int y = 0; y < height; y++) {
							writer.setArgb(x, y, bufferedImage.getRGB(x, y) & mask);
						}
					}
					
					imageView.setImage(newImage);
				}
			}
		}
		
	}
	
	private Image loadImage(ProjectItem item) throws IOException {
		imageType = item.getSpecificExtension().toLowerCase();
		file = item.getFile();
		bufferedImage = null;
		
		switch (imageType) {
		case "png":
		case "jpg":
		case "jpeg":
			try (FileInputStream is = new FileInputStream(file)) {
				return new Image(is);
			}
		case "dds":
			bufferedImage = DDSTexture.toBufferedImage(file);
			return SwingFXUtils.toFXImage(bufferedImage, null);
		case "rw4":
			bufferedImage = RenderWare.fromFile(file).toTexture().toBufferedImage();
			return SwingFXUtils.toFXImage(bufferedImage, null);
		case "rast":
		case "raster":
			bufferedImage = RasterTexture.textureFromFile(file).toBufferedImage();
			return SwingFXUtils.toFXImage(bufferedImage, null);
		case "bitimage":
		case "8bitimage":
		case "32bitimage":
		case "48bitimage":
			return BitmapImage.readImage(file).getImage();
		default:
			return null;
		}
	}

	@Override
	public void loadFile(ProjectItem item) throws IOException {
		if (item != null) {
			
			originalImage = loadImage(item);
			
			imageView.setImage(originalImage);
			
			inspector.getChildren().clear();
			
			inspector.getChildren().add(exportAsPNG);
			inspector.getChildren().add(exportAsDDS);
			inspector.getChildren().add(new Separator(Orientation.HORIZONTAL));
			
			inspector.getChildren().add(new Label("Zoom:"));
			inspector.getChildren().add(zoomLvlSpinner);
			
			ColorPicker backgroundColor = new ColorPicker();
			backgroundColor.setPrefWidth(Double.MAX_VALUE);
			backgroundColor.setOnAction((event) -> {
				Color color = backgroundColor.getValue();
				imagePane.setStyle("-fx-background-color: " + String.format( "#%02X%02X%02X",
			            (int)(color.getRed() * 255 ),
			            (int)(color.getGreen() * 255 ),
			            (int)(color.getBlue() * 255 ) ));
			});
			backgroundColor.setValue(Color.web("#f2f2f2"));
			
			inspector.getChildren().add(new Label("Background Color:"));
			inspector.getChildren().add(backgroundColor);
			
			inspector.getChildren().add(new Label("Channels:"));
			inspector.getChildren().add(cbRedMask);
			inspector.getChildren().add(cbGreenMask);
			inspector.getChildren().add(cbBlueMask);
			inspector.getChildren().add(cbAlphaMask);
			
			if (imageType.equals("rw4")) {
				Label label = new Label("RW4 textures cannot be directly used outside SporeModder, you must convert them first.");
				label.setGraphic(UIManager.get().getAlertIcon(AlertType.WARNING, 16, 16));
				
				UIManager.get().getUserInterface().setStatusInfo(label);
			}
			
			if (!imageType.equals("rw4") && !imageType.equals("dds") && !imageType.equals("rast") && !imageType.equals("raster")) {
				exportAsDDS.setVisible(false);
			}
			
			showInspector();
		}
	}
	
	private void exportPNG() {
		UIManager.get().setOverlay(true);
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(FileManager.FILEFILTER_PNG);
		chooser.getExtensionFilters().add(FileManager.FILEFILTER_ALL);
		File result = chooser.showSaveDialog(UIManager.get().getScene().getWindow());
		UIManager.get().setOverlay(false);
		
		if (result != null) {
			if (result.getName().indexOf(".") == -1) {
				// There is no extension, add the png one
				result = new File(result.getParentFile(), result.getName() + ".png");
			}
			
			try {
				if (imageType.equals("png")) {
					Files.copy(file.toPath(), result.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} else {
					ImageIO.write(SwingFXUtils.fromFXImage(originalImage, null), "PNG", result);
				}
			} 
			catch (IOException e) {
				UIManager.get().showDialog(new Alert(AlertType.ERROR, "The image could not be exported: " + e.getLocalizedMessage(), ButtonType.OK));
			}
		}
	}
	
	private void exportDDS() {
		UIManager.get().setOverlay(true);
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(FileManager.FILEFILTER_DDS);
		chooser.getExtensionFilters().add(FileManager.FILEFILTER_ALL);
		File result = chooser.showSaveDialog(UIManager.get().getScene().getWindow());
		UIManager.get().setOverlay(false);
		
		if (result != null) {
			if (result.getName().indexOf(".") == -1) {
				// There is no extension, add the dds one
				result = new File(result.getParentFile(), result.getName() + ".dds");
			}
			
			try {
				switch(imageType) {
				case "dds": Files.copy(file.toPath(), result.toPath(), StandardCopyOption.REPLACE_EXISTING); break;
				case "rw4": RenderWare.toTexture(file, result); break;
				case "rast":
				case "raster":
					RasterTexture.textureFromFile(file, result); break;
				}
			}
			catch (Exception e) {
				UIManager.get().showDialog(new Alert(AlertType.ERROR, "The image could not be exported: " + e.getLocalizedMessage(), ButtonType.OK));
			}
		}
	}
	
	private void showInspector() {
		UserInterface.get().getInspectorPane().configureDefault("Image Viewer", "image", inspector);
	}
	private void hideInspector() {
		 UserInterface.get().getInspectorPane().reset();
	}

	@Override
	public void setActive(boolean isActive) {
		if (isActive) {
			showInspector();
		} else {
			hideInspector();
		}
	}

	@Override
	public Node getUI() {
		return mainNode;
	}

	@Override
	public void save() {
	}

	@Override
	public boolean isEditable() {
		// Images are not editable
		return false;
	}

	@Override
	public void setDestinationFile(File file) {
		this.file = file;
	}

	@Override
	public boolean supportsSearching() {
		return false;
	}

	@Override
	public boolean supportsEditHistory() {
		return false;
	}
}
