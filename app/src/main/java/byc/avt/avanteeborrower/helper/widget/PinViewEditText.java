package byc.avt.avanteeborrower.helper.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import byc.avt.avanteeborrower.R;

import static android.text.InputType.TYPE_CLASS_NUMBER;
import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD;

public class PinViewEditText extends LinearLayout implements TextWatcher, View.OnFocusChangeListener, View.OnKeyListener {
    private final float DENSITY = getContext().getResources().getDisplayMetrics().density;
    /**
     * Attributes
     */
    private int mPinLength = 4;
    private List<EditText> editTextList = new ArrayList<>();
    private int mPinWidth = 50;
    private int mTextSize = 28;
    private int mPinHeight = 50;
    private int mSplitWidth = 20;
    private boolean mCursorVisible = false;
    private boolean mDelPressed = false;
    @DrawableRes
    private int mPinBackground = R.drawable.bg_white_rounded_8;
    private boolean mPassword = false;
    private String mHint = "";
    private InputType inputType = InputType.NUMBER;
    private boolean finalNumberPin = false;
    private boolean fromSetValue = false;
    private boolean mForceKeyboard = true;

    private PinViewEventListener mListener;
    OnClickListener mClickListener;

    View currentFocus = null;

    InputFilter filters[] = new InputFilter[1];
    LinearLayout.LayoutParams params;

    public PinViewEditText(Context context) {
        this(context, null);
    }

    public PinViewEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PinViewEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setGravity(Gravity.CENTER);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        this.removeAllViews();
        mPinHeight *= DENSITY;
        mPinWidth *= DENSITY;
        mSplitWidth *= DENSITY;
        setWillNotDraw(false);
        initAttributes(context, attrs, defStyleAttr);
        params = new LayoutParams(mPinWidth, mPinHeight);
        setOrientation(HORIZONTAL);
        createEditTexts();
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean focused = false;
                for (EditText editText : editTextList) {
                    if (editText.length() == 0) {
                        editText.requestFocus();
                        openKeyboard();
                        focused = true;
                        break;
                    }
                }
                if (!focused && editTextList.size() > 0) { // Focus the last view
                    editTextList.get(editTextList.size() - 1).requestFocus();
                }
                if (mClickListener != null) {
                    mClickListener.onClick(PinViewEditText.this);
                }
            }
        });
        // Bring up the keyboard
        final View firstEditText = editTextList.get(0);
        if (firstEditText != null)
            firstEditText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    openKeyboard();
                }
            }, 200);
        updateEnabledState();
    }

    private void createEditTexts() {
        removeAllViews();
        editTextList.clear();
        EditText editText;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        for (int i = 0; i < mPinLength; i++) {
            editText = new EditText(getContext());
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
            //editText.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/Roboto-Regular.ttf")); TODO for set font
            editText.setTextSize(mTextSize);
            editText.setId(R.id.edit_text_pin_column);
            editTextList.add(i, editText);
            this.addView(editText);
            generateOneEditText(editText, "" + i);
        }
        setTransformation();
    }

    private void initAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.Pinview, defStyleAttr, 0);
            mPinBackground = array.getResourceId(R.styleable.Pinview_pinBackground, mPinBackground);
            mPinLength = array.getInt(R.styleable.Pinview_pinLength, mPinLength);
            mPinHeight = (int) array.getDimension(R.styleable.Pinview_pinHeight, mPinHeight);
            mPinWidth = (int) array.getDimension(R.styleable.Pinview_pinWidth, mPinWidth);
            mSplitWidth = (int) array.getDimension(R.styleable.Pinview_splitWidth, mSplitWidth);
            // mTextSize = (int) array.getDimension(R.styleable.Pinview_textSize, mTextSize);
            mCursorVisible = array.getBoolean(R.styleable.Pinview_cursorVisible, mCursorVisible);
            mPassword = array.getBoolean(R.styleable.Pinview_password, mPassword);
            mForceKeyboard = array.getBoolean(R.styleable.Pinview_forceKeyboard, mForceKeyboard);
            mHint = array.getString(R.styleable.Pinview_hint);
            //InputType[] its = InputType.values();
            //inputType = its[array.getInt(R.styleable.Pinview_inputType, 0)];
            array.recycle();
        }
    }

    private void generateOneEditText(EditText styleEditText, String tag) {
        params.setMargins(mSplitWidth / 2, mSplitWidth / 2, mSplitWidth / 2, mSplitWidth / 2);
        filters[0] = new InputFilter.LengthFilter(1);
        styleEditText.setFilters(filters);
        styleEditText.setLayoutParams(params);
        styleEditText.setGravity(Gravity.CENTER);
        styleEditText.setCursorVisible(mCursorVisible);

        if (!mCursorVisible) {
            styleEditText.setClickable(false);
            styleEditText.setHint(mHint);

            styleEditText.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    // When back space is pressed it goes to delete mode and when u click on an edit Text it should get out of the delete mode
                    mDelPressed = false;
                    return false;
                }
            });
        }
        styleEditText.setBackgroundResource(mPinBackground);
        styleEditText.setPadding(0, 0, 0, 0);
        styleEditText.setTag(tag);
        styleEditText.setInputType(getKeyboardInputType());
        styleEditText.addTextChangedListener(this);
        styleEditText.setOnFocusChangeListener(this);
        styleEditText.setOnKeyListener(this);
    }

    private int getKeyboardInputType() {
        int it;
        switch (inputType) {
            case NUMBER:
                it = TYPE_CLASS_NUMBER | TYPE_NUMBER_VARIATION_PASSWORD;
                break;
            case TEXT:
                it = TYPE_CLASS_TEXT;
                break;
            default:
                it = TYPE_CLASS_TEXT;
        }
        return it;
    }

    public String getValue() {
        StringBuilder sb = new StringBuilder();
        for (EditText et : editTextList) {
            sb.append(et.getText().toString());
        }
        return sb.toString();
    }

    public View requestPinEntryFocus() {
        int currentTag = Math.max(0, getIndexOfCurrentFocus());
        EditText currentEditText = editTextList.get(currentTag);
        if (currentEditText != null) {
            currentEditText.requestFocus();
        }
        openKeyboard();
        return currentEditText;
    }

    private void openKeyboard() {
        if (mForceKeyboard) {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    public void clearValue() {
        setValue("");
    }

    public void setValue(@NonNull String value) {
        String regex = "[0-9]*"; // Allow empty string to clear the fields
        fromSetValue = true;
        if (inputType == InputType.NUMBER && !value.matches(regex))
            return;
        int lastTagHavingValue = -1;
        for (int i = 0; i < editTextList.size(); i++) {
            if (value.length() > i) {
                lastTagHavingValue = i;
                editTextList.get(i).setText(((Character) value.charAt(i)).toString());
            } else {
                editTextList.get(i).setText("");
            }
        }
        if (mPinLength > 0) {
            if (lastTagHavingValue < mPinLength - 1) {
                currentFocus = editTextList.get(lastTagHavingValue + 1);
            } else {
                currentFocus = editTextList.get(mPinLength - 1);
                if (inputType == InputType.NUMBER || mPassword)
                    finalNumberPin = true;
                if (mListener != null)
                    mListener.onDataEntered(this, false);
            }
            currentFocus.requestFocus();
        }
        fromSetValue = false;
        updateEnabledState();
    }

    private void setTransformation() {
        if (mPassword) {
            for (EditText editText : editTextList) {
                editText.removeTextChangedListener(this);
                editText.setTransformationMethod(new PinTransformationMethod());
                editText.addTextChangedListener(this);
            }
        } else {
            for (EditText editText : editTextList) {
                editText.removeTextChangedListener(this);
                editText.setTransformationMethod(null);
                editText.addTextChangedListener(this);
            }
        }
    }

    private void updateEnabledState() {
        int currentTag = Math.max(0, getIndexOfCurrentFocus());
        for (int index = 0; index < editTextList.size(); index++) {
            EditText editText = editTextList.get(index);
            editText.setEnabled(index <= currentTag);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int i1, int count) {
        if (charSequence.length() == 1 && currentFocus != null) {
            final int currentTag = getIndexOfCurrentFocus();
            if (currentTag < mPinLength - 1) {
                long delay = 1;
                if (mPassword)
                    delay = 25;
                this.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        EditText nextEditText = editTextList.get(currentTag + 1);
                        nextEditText.setEnabled(true);
                        nextEditText.requestFocus();
                    }
                }, delay);
            } else {
                //Last Pin box has been reached.
            }
            if (currentTag == mPinLength - 1 && inputType == InputType.NUMBER || currentTag == mPinLength - 1 && mPassword) {
                finalNumberPin = true;
            }

        } else if (charSequence.length() == 0) {
            int currentTag = getIndexOfCurrentFocus();
            mDelPressed = true;
            //For the last cell of the non password text fields. Clear the text without changing the focus.
            if (editTextList.get(currentTag).getText().length() > 0)
                editTextList.get(currentTag).setText("");
        }

        for (int index = 0; index < mPinLength; index++) {
            if (editTextList.get(index).getText().length() < 1)
                break;
            if (!fromSetValue && index + 1 == mPinLength && mListener != null)
                mListener.onDataEntered(this, true);
        }
        updateEnabledState();
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onFocusChange(View view, boolean isFocused) {
        if (isFocused && !mCursorVisible) {
            if (mDelPressed) {
                currentFocus = view;
                mDelPressed = false;
                return;
            }
            for (final EditText editText : editTextList) {
                if (editText.length() == 0) {
                    if (editText != view) {
                        editText.requestFocus();
                    } else {
                        currentFocus = view;
                    }
                    return;
                }
            }
            if (editTextList.get(editTextList.size() - 1) != view) {
                editTextList.get(editTextList.size() - 1).requestFocus();
            } else {
                currentFocus = view;
            }
        } else if (isFocused && mCursorVisible) {
            currentFocus = view;
        } else {
            view.clearFocus();
        }
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if ((keyEvent.getAction() == KeyEvent.ACTION_UP) && (i == KeyEvent.KEYCODE_DEL)) {
            // Perform action on Del press
            int currentTag = getIndexOfCurrentFocus();
            //Last tile of the number pad. Clear the edit text without changing the focus.
            if (inputType == InputType.NUMBER && currentTag == mPinLength - 1 && finalNumberPin ||
                    (mPassword && currentTag == mPinLength - 1 && finalNumberPin)) {
                if (editTextList.get(currentTag).length() > 0) {
                    editTextList.get(currentTag).setText("");
                }
                finalNumberPin = false;
            } else if (currentTag > 0) {
                mDelPressed = true;
                if (editTextList.get(currentTag).length() == 0) {
                    //Takes it back one tile
                    editTextList.get(currentTag - 1).requestFocus();
                    //Clears the tile it just got to
                    editTextList.get(currentTag).setText("");
                } else {
                    //If it has some content clear it first
                    editTextList.get(currentTag).setText("");
                }
            } else {
                //For the first cell
                if (editTextList.get(currentTag).getText().length() > 0)
                    editTextList.get(currentTag).setText("");
            }
            return true;

        }

        return false;
    }

    private int getIndexOfCurrentFocus() {
        return editTextList.indexOf(currentFocus);
    }


    public int getSplitWidth() {
        return mSplitWidth;
    }

    public void setSplitWidth(int splitWidth) {
        this.mSplitWidth = splitWidth;
        int margin = splitWidth / 2;
        params.setMargins(margin, margin, margin, margin);

        for (EditText editText : editTextList) {
            editText.setLayoutParams(params);
        }
    }

    public int getPinHeight() {
        return mPinHeight;
    }

    public void setPinHeight(int pinHeight) {
        this.mPinHeight = pinHeight;
        params.height = pinHeight;
        for (EditText editText : editTextList) {
            editText.setLayoutParams(params);
        }
    }

    public int getPinWidth() {
        return mPinWidth;
    }

    public void setPinWidth(int pinWidth) {
        this.mPinWidth = pinWidth;
        params.width = pinWidth;
        for (EditText editText : editTextList) {
            editText.setLayoutParams(params);
        }
    }

    public int getPinLength() {
        return mPinLength;
    }

    public void setPinLength(int pinLength) {
        this.mPinLength = pinLength;
        createEditTexts();
    }

    public boolean isPassword() {
        return mPassword;
    }

    public void setPassword(boolean password) {
        this.mPassword = password;
        setTransformation();
    }

    public String getHint() {
        return mHint;
    }

    public void setHint(String mHint) {
        this.mHint = mHint;
        for (EditText editText : editTextList)
            editText.setHint(mHint);
    }

    public
    @DrawableRes
    int getPinBackground() {
        return mPinBackground;
    }

    public void setPinBackgroundRes(@DrawableRes int res) {
        this.mPinBackground = res;
        for (EditText editText : editTextList)
            editText.setBackgroundResource(res);
    }

    public void setInputType(InputType inputType) {
        this.inputType = inputType;
        int it = getKeyboardInputType();
        for (EditText editText : editTextList) {
            editText.setInputType(it);
        }
    }

    public void setPinViewEventListener(PinViewEventListener listener) {
        this.mListener = listener;
    }

    private class PinTransformationMethod implements TransformationMethod {

        private char BULLET = '\u2022';

        @Override
        public CharSequence getTransformation(CharSequence source, final View view) {
            return new PasswordCharSequence(source);
        }

        @Override
        public void onFocusChanged(final View view, final CharSequence sourceText, final boolean focused, final int direction, final Rect previouslyFocusedRect) {

        }

        private class PasswordCharSequence implements CharSequence {
            private final CharSequence source;

            public PasswordCharSequence(@NonNull CharSequence source) {
                this.source = source;
            }

            @Override
            public int length() {
                return source.length();
            }

            @Override
            public char charAt(int index) {
                return BULLET;
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return new PasswordCharSequence(this.source.subSequence(start, end));
            }

        }
    }

    public InputType getInputType() {
        return inputType;
    }

    public enum InputType {
        TEXT, NUMBER
    }

    public interface PinViewEventListener {
        void onDataEntered(PinViewEditText pinViewEditText, boolean fromUser);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mClickListener = l;
    }
}
