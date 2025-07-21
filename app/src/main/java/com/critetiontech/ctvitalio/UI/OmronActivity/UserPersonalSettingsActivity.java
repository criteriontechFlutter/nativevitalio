package com.critetiontech.ctvitalio.UI.OmronActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


import com.critetiontech.ctvitalio.R;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UserPersonalSettingsActivity extends BaseActivity {
    private TextView tvDateOfBirth;
    private EditText etHeight;
    private EditText etWeight;
    private EditText etWStride;
    private String dateString;
    private RadioGroup unitRadio;
    private RadioGroup genderRadio;
    private final Calendar calendar = Calendar.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        //init view
        setContentView(R.layout.activity_user_personal_settings);
        tvDateOfBirth = findViewById(R.id.tv_date);
        etHeight = setInputFilter(imm, R.id.view_height,R.id.et_height,personalData.getHeight(),"100.00","220.00");
        etWeight = setInputFilter(imm, R.id.view_Weight,R.id.et_Weight,personalData.getWeight(),"10.00","250.00");
        etWStride = setInputFilter(imm, R.id.stride_length,R.id.et_stride,personalData.getStride(),"30.00","120.00");
        unitRadio = findViewById(R.id.unitRadio);
        genderRadio = findViewById(R.id.genderRadio);
        setRadioButton(imm,R.id.kgRadioButton,OmronConstants.OMRONDeviceWeightUnit.Kg,true);
        setRadioButton(imm,R.id.lbsRadioButton,OmronConstants.OMRONDeviceWeightUnit.Lbs,true);
        //I want to delete it at the destination, but there are no conditions that allow me to make a decision, so I delete it all the time.;
        setRadioButton(imm,R.id.stRadioButton, OmronConstants.OMRONDeviceWeightUnit.St,false);
        setRadioButton(imm,R.id.MaleRadioButton,OmronConstants.OMRONDevicePersonalSettingsUserGenderType.Male,true);
        setRadioButton(imm,R.id.FemaleRadioButton,OmronConstants.OMRONDevicePersonalSettingsUserGenderType.Female,true);
        final Button btSave = findViewById(R.id.bt_save);
        Date dateTime = getDateOfBirthDateType( personalData.getBirthday(),"yyyy/MM/dd");
        tvDateOfBirth.setText(getDate(dateTime));
        calendar.set(Calendar.YEAR, personalData.getYear());
        calendar.set(Calendar.MONTH, personalData.getMonth() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, personalData.getDay());
        int selectUnit;
        switch(personalData.getUnitValue()){
            case OmronConstants.OMRONDeviceWeightUnit.Kg:
                selectUnit = R.id.kgRadioButton;
                break;
            case OmronConstants.OMRONDeviceWeightUnit.Lbs:
                selectUnit = R.id.lbsRadioButton;
                break;
            default:
                selectUnit = R.id.stRadioButton;
                break;
        }
        unitRadio.check(selectUnit);
        int selectGender;
        switch(personalData.getGenderValue()){
            case OmronConstants.OMRONDevicePersonalSettingsUserGenderType.Female:
                selectGender = R.id.FemaleRadioButton;
                break;
            default:
                selectGender = R.id.MaleRadioButton;
                break;
        }
        genderRadio.check(selectGender);

        tvDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFocus();
                new DatePickerDialog(UserPersonalSettingsActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, day);
                        dateString = format.format(calendar.getTime());
                        personalData.setBirthday(dateString);
                        Date dateTime = getDateOfBirthDateType(dateString,"yyyy/MM/dd");
                        tvDateOfBirth.setText(getDate(dateTime));
                    }
                }, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        unitRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                removeFocus();
            }
        });

        genderRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                removeFocus();
            }
        });
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFocus();
                personalData.setHeight(String.valueOf(etHeight.getText()));
                personalData.setWeight(String.valueOf(etWeight.getText()));
                personalData.setStride(String.valueOf(etWStride.getText()));
                int selectedUnitId = unitRadio.getCheckedRadioButtonId();
                int selectedGenderId = genderRadio.getCheckedRadioButtonId();
                RadioButton selectedUnitButton = findViewById(selectedUnitId);
                RadioButton selectedGenderButton = findViewById(selectedGenderId);
                personalData.setUnitValue((int) selectedUnitButton.getTag());
                personalData.setGenderValue((int) selectedGenderButton.getTag());
                personalData.savePersonalData();

                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
        removeFocus();
    }
    private void removeFocus(){
        View parent = findViewById(R.id.top_bar);
        parent.setFocusable(true);
        parent.setFocusableInTouchMode(true);
        parent.requestFocus();
    }

    private EditText setInputFilter(final InputMethodManager imm, final int viewID, final int editID, final String now, final String min, final String max) {
        final LinearLayout view = findViewById(viewID);
        final EditText editText = findViewById(editID);
        final String[] statText = {"",""};
        final double minVal = Double.parseDouble(min);
        final double maxVal = Double.parseDouble(max);
        final int numberOfIntegers = max.split("\\.")[0].length();
        final int numberOfDecimals = max.split("\\.")[1].length();
        StringBuilder zeros = new StringBuilder(numberOfDecimals);
        for(int i = 0; i < numberOfDecimals; i++) {
            zeros.append('0');
        }
        final DecimalFormat decimalFormat = new DecimalFormat("0." + zeros, new DecimalFormatSymbols(Locale.US));
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editText.setText(now);

        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String inputText = editText.getText().toString();
                 if (inputText.contains(".")) {
                    String[] splitByDecimal = inputText.split("\\.");
                    if (splitByDecimal.length == 0 ||
                        (splitByDecimal.length == 1 && splitByDecimal[0].length() > numberOfIntegers) ||
                        (splitByDecimal.length == 2 && (splitByDecimal[0].length() > numberOfIntegers || splitByDecimal[1].length() > numberOfDecimals))) {
                        editText.setText(statText[1]);
                        editText.setSelection(statText[1].length());
                    } else {
                        statText[1] = inputText;
                    }
                } else if (inputText.length() > numberOfIntegers) {
                    editText.setText(statText[1]);
                    editText.setSelection(statText[1].length());
                } else {
                    statText[1] = inputText;
                }
            }
        });

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String inputText = editText.getText().toString();
                    if (!inputText.isEmpty()) {
                        String setText;
                        double inputValue = Double.parseDouble(inputText);
                        if (inputValue < minVal) {
                            setText = decimalFormat.format(minVal);
                        } else if (inputValue > maxVal) {
                            setText = decimalFormat.format(maxVal);
                        } else {
                            setText = decimalFormat.format(inputValue);
                        }
                        editText.setText(new BigDecimal(setText).stripTrailingZeros().toPlainString());
                    } else {
                        editText.setText(statText[0]);
                    }
                } else {
                    statText[0] = editText.getText().toString();
                    editText.setSelection(statText[0].length());
                }
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    removeFocus();
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
                return true;
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setEnabled(true);
                editText.requestFocus();
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        return editText;
    }
    private void setRadioButton(final InputMethodManager imm, int id, int tag, boolean isShow){
        final RadioButton button = findViewById(id);
        if(isShow) {
            button.setTag(tag);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imm.hideSoftInputFromWindow(button.getWindowToken(), 0);
                    removeFocus();
                }
            });
        }else{
            button.setVisibility(View.GONE);
        }
    }
}
