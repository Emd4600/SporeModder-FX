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
package sporemodder.file.argscript.inspector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import sporemodder.HashManager;
import sporemodder.file.DocumentException;
import sporemodder.file.argscript.ArgScriptLexer;
import sporemodder.util.Vector2;

public class ASVector2Inspector extends ASValueInspector {
	
	private static final String[] TAGS = new String[] {"X", "Y"};
	
	private class CustomStringConverter extends StringConverter<Double> {
		
		private int index;
		
		private CustomStringConverter(int index) {
			this.index = index;
		}

		@Override
		public Double fromString(String arg0) {
			if (arg0.trim().isEmpty()) {
				return (double) defaultValue[index];
			}
			
			ArgScriptLexer lexer = new ArgScriptLexer();
			lexer.setText(arg0);
			try {
				return lexer.parseFloat();
			} catch (DocumentException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String toString(Double value) {
			if (value == null) return "";
			return HashManager.get().floatToString(value.floatValue());
		}
		
	};
	
	private final float[] currentValue = new float[2];
	private final float[] defaultValue = new float[2];
	private double minValue;
	private double maxValue;
	private double step;
	private boolean showTags = true;
	
	private final List<Spinner<Double>> spinners = new ArrayList<Spinner<Double>>();
	
	private final List<StringConverter<Double>> stringConverters = new ArrayList<StringConverter<Double>>();
	
	public ASVector2Inspector(String name, String descriptionCode, float defaultValue) {
		this(name, descriptionCode, new Vector2(defaultValue, defaultValue));
	}
	
	public ASVector2Inspector(String name, String descriptionCode, Vector2 defaultValue) {
		super(name, descriptionCode);
		this.defaultValue[0] = defaultValue.getX();
		this.defaultValue[1] = defaultValue.getY();
		this.minValue = -Double.MAX_VALUE;
		this.maxValue = Double.MAX_VALUE;
		this.step = 0.1f;
		
		for (int i = 0; i < currentValue.length; i++) {
			currentValue[i] = this.defaultValue[i];
		}

		stringConverters.add(new CustomStringConverter(0));
		stringConverters.add(new CustomStringConverter(1));
		stringConverters.add(new CustomStringConverter(2));
	}
	
	public ASVector2Inspector setRange(float max, float min) {
		this.maxValue = max;
		this.minValue = min;
		return this;
	}
	
	public ASVector2Inspector setStep(float step) {
		this.step = step;
		return this;
	}
	
	public ASVector2Inspector setShowTags(boolean showTags) {
		this.showTags = showTags;
		return this;
	}
	
	@Override
	public int getArgumentCount() {
		return 1;
	}
	
	private String vectorToString(Vector2 vector) {
		return stringConverters.get(0).toString((double) vector.getX()) + ", "
				+ stringConverters.get(1).toString((double) vector.getY());
	}
	
	@Override
	public void generateUI(VBox panel) {
		super.generateUI(panel);
		
		
		float[] initialValue = Arrays.copyOf(defaultValue, defaultValue.length);
		
		// Only do this if the option exists
		if (getArguments() != null) {
			lineInspector.getStream().parseVector2(getArguments(), argIndex, initialValue);
		}
		
		spinners.clear();
		for (int i = 0; i < 2; i++) {
			Spinner<Double> spinner = new Spinner<Double>();
			spinners.add(spinner);
			spinner.setPrefWidth(Double.MAX_VALUE);
			
			SpinnerValueFactory.DoubleSpinnerValueFactory valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(minValue, maxValue);
			spinner.setValueFactory(valueFactory);
			
			valueFactory.setAmountToStepBy(step);
			valueFactory.setConverter(stringConverters.get(i));
			valueFactory.setValue((double) initialValue[i]);
			
			spinner.setEditable(true);
			spinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
				if (!newValue) {
					spinner.increment(0); // won't change value, but will commit editor
				}
			});
			
			int index = i;
			spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
				
				if (newValue == null) {
					newValue = (double) defaultValue[index];
				}
				
				currentValue[index] = newValue.floatValue();
				
				// If it's not an option, it will come after the keyword
				int splitIndex = argIndex + createIfNecessary();
				
				lineInspector.getSplits().set(splitIndex, vectorToString(new Vector2(currentValue)));
				
				this.submitChanges();
			});
			
			if (showTags) {
				BorderPane tagPane = new BorderPane();
				Label label = new Label(TAGS[i] + ": ");
				tagPane.setLeft(label);
				tagPane.setCenter(spinner);
				BorderPane.setAlignment(label, Pos.CENTER);
				panel.getChildren().add(tagPane);
			}
			else {
				panel.getChildren().add(spinner);
			}
		}
	}

	@Override
	void addDefaultValue(List<String> splits) {
		splits.add(vectorToString(new Vector2(defaultValue)));
	}
	
	@Override
	int getRemoveableCount() {
		return 1;
	}
	
	@Override
	boolean isDefault() {
		if (getArguments() != null) {
			float[] values = new float[2];
			if (!lineInspector.getStream().parseVector2(getArguments(), argIndex, values)) {
				return false;
			}
			
			if (values[0] != defaultValue[0] || values[1] != defaultValue[1]) {
				return false;
			}
			
			return true;
		}
		else {
			return true;
		}
	}
	
	@Override
	public void setEnabled(boolean isEnabled) {
		for (Spinner<Double> spinner : spinners) {
			spinner.setDisable(!isEnabled);
		}
	}
}