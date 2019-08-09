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
package sporemodder.view.inspector;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;
import sporemodder.HashManager;
import sporemodder.file.DocumentException;
import sporemodder.file.argscript.ArgScriptLexer;

public class InspectorIntSpinner extends Spinner<Long> implements InspectorValue<Long> {
	
	private static class LongSpinnerValueFactory extends SpinnerValueFactory<Long> {
		
		private long min = Long.MIN_VALUE;
		private long max = Long.MAX_VALUE;
		private long step = 1;
		
		public LongSpinnerValueFactory(long initialValue) {
			
			valueProperty().addListener((o, oldValue, newValue) -> {
                // when the value is set, we need to react to ensure it is a
                // valid value (and if not, blow up appropriately)
                if (newValue < min) {
                    setValue(min);
                } else if (newValue > max) {
                	setValue(max);
            	}
            });
			
			setValue(initialValue >= min && initialValue <= max ? initialValue : min);
		}

		@Override
		public void decrement(int steps) {
            final long newIndex = getValue() - steps * step;
            setValue(newIndex >= min ? newIndex : min);
		}

		@Override
		public void increment(int steps) {
			final long newIndex = getValue() + steps * step;
            setValue(newIndex <= max ? newIndex : max);
		}
		
		
	};
	
	private long defaultValue = 0;
	
	private final LongSpinnerValueFactory valueFactory;
	
	public InspectorIntSpinner() {
		this(0);
	}
	
	public InspectorIntSpinner(long defaultValue) {
		super();
		
		this.defaultValue = defaultValue;
		
		valueFactory = new LongSpinnerValueFactory(defaultValue);
		setValueFactory(valueFactory);
		
		valueFactory.setConverter(new StringConverter<Long>() {
			
			private String lastString;
			private Long lastValue;

			@Override
			public Long fromString(String arg0) {
				if (arg0.trim().isEmpty()) {
					return InspectorIntSpinner.this.defaultValue;
				}
				
				ArgScriptLexer lexer = new ArgScriptLexer();
				lexer.addFunction("hash", ArgScriptLexer.FUNCTION_HASH);
				lexer.setText(arg0);
				try {
					lastValue = lexer.parseInteger();
					lastString = arg0;
					return lastValue;
				} catch (DocumentException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public String toString(Long value) {
				if (value == null) return "";
				if (value == lastValue) return lastString;
				if (valueFactory.max <= Integer.MAX_VALUE) {
					return HashManager.get().formatInt32((int) (value & 0xFFFFFFFFL));
				} else {
					return HashManager.get().formatInt64(value);
				}
			}
			
		});
		valueFactory.setValue(defaultValue);
		
		setEditable(true);
		
		// Ensure the value is saved when we exit focus
		focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				increment(0); // won't change value, but will commit editor
			}
		});
		
		this.getEditor().setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.UP) {
					setValue(getValue() + getStep());
					event.consume();
				}
				else if (event.getCode() == KeyCode.DOWN) {
					setValue(getValue() - getStep());
					event.consume();
				}
			}
		});
	}
	
	/**
	 * Sets the minimum and maximum values allowed for this inspector value. If this method is not used,
	 * there is no range by default.
	 * @param min
	 * @param max
	 * @return
	 */
	public InspectorIntSpinner setRange(long min, long max) {
		valueFactory.min = min;
		valueFactory.max = max;
		
		return this;
	}
	
	/**
	 * Sets the quantity that is increased/decreased every time the value spinner buttons are used.
	 * @param step
	 * @return
	 */
	public InspectorIntSpinner setStep(long step) {
		valueFactory.step = step;
		return this;
	}
	
	public long getMinValue() {
		return valueFactory.min;
	}
	
	public long getMaxValue() {
		return valueFactory.max;
	}
	
	public long getStep() {
		return valueFactory.step;
	}
	
	public void setDefaultValue(long defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public long getDefaultValue() {
		return defaultValue;
	}

	@Override public void setValue(Long value) {
		valueFactory.setValue(value);
	}
	
	public void setValue(String value) {
		valueFactory.setValue(valueFactory.getConverter().fromString(value));
	}

	@Override
	public Node getNode() {
		return this;
	}

	@Override
	public void addValueListener(ChangeListener<Long> listener) {
		this.valueProperty().addListener(listener);
	}

	@Override
	public void removeValueListener(ChangeListener<Long> listener) {
		this.valueProperty().removeListener(listener);
	}
}
