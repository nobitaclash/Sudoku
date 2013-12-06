package pis.android.sudoku.widget;

import pis.android.sudoku.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewWithFont extends TextView {

	public TextViewWithFont(Context context) {
		super(context);
	}

	public TextViewWithFont(Context context, AttributeSet attrs) {
		super(context, attrs, android.R.attr.textViewStyle);
		customFont(context, attrs);
	}

	public TextViewWithFont(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		customFont(context, attrs);
	}

	public void customFont(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ViewWithFont, android.R.attr.buttonStyle, 0);
		String font = a.getString(R.styleable.ViewWithFont_textFont);
		Typeface typeface = Typeface.createFromAsset(context.getAssets(),
				"fonts/" + font);
		this.setTypeface(typeface);
		a.recycle();
	}
}
