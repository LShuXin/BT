
package com.lsx.bigtalk.ui.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.utils.CommonUtil;
import com.lsx.bigtalk.logs.Logger;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description 表情解析
 * @author Nana
 * @date 2014-4-16
 */
@SuppressLint("UseSparseArrays")
public class Emoparser {
    private static final Logger logger = Logger.getLogger(Emoparser.class);

    private final Context context;
    private final String[] emoList;
    private final String[] yayaEmoList;
    private final Pattern mPattern;
    private final Pattern mYayaPattern;
    private static HashMap<String, Integer> phraseIdMap;
    private static HashMap<Integer, String> idPhraseMap;
    private static HashMap<String, Integer> yayaPhraseIdMap;
    private static HashMap<Integer, String> yayaIdPhraseMap;
    private static Emoparser instance = null;

    public static boolean isGifEmo = false;
    private final int DEFAULT_SMILEY_TEXTS = R.array.default_emoji_phrase;
    private final int YAYA_EMO_TEXTS = R.array.yaya_emoji_phrase;
    private final int[] DEFAULT_EMO_RES_IDS = {
            R.drawable.ic_emoji00, R.drawable.ic_emoji01,
            R.drawable.ic_emoji02, R.drawable.ic_emoji03, R.drawable.ic_emoji04, R.drawable.ic_emoji05,
            R.drawable.ic_emoji06, R.drawable.ic_emoji07, R.drawable.ic_emoji08, R.drawable.ic_emoji09,
            R.drawable.ic_emoji10, R.drawable.ic_emoji11, R.drawable.ic_emoji12, R.drawable.ic_emoji13,
            R.drawable.ic_emoji14, R.drawable.ic_emoji15, R.drawable.ic_emoji16, R.drawable.ic_emoji17,
            R.drawable.ic_emoji18, R.drawable.ic_emoji19, R.drawable.ic_emoji20, R.drawable.ic_emoji21,
            R.drawable.ic_emoji22, R.drawable.ic_emoji23, R.drawable.ic_emoji24, R.drawable.ic_emoji25,
            R.drawable.ic_emoji26, R.drawable.ic_emoji27, R.drawable.ic_emoji28, R.drawable.ic_emoji29,
            R.drawable.ic_emoji30, R.drawable.ic_emoji31, R.drawable.ic_emoji32, R.drawable.ic_emoji33,
            R.drawable.ic_emoji34, R.drawable.ic_emoji35, R.drawable.ic_emoji36, R.drawable.ic_emoji37,
            R.drawable.ic_emoji38, R.drawable.ic_emoji39, R.drawable.ic_emoji40, R.drawable.ic_emoji41,
            R.drawable.ic_emoji42, R.drawable.ic_emoji43, R.drawable.ic_emoji44, R.drawable.ic_emoji45
    };

    private final int[] YAYA_EMO_RES_IDS = {
            R.drawable.yaya_emoji01, R.drawable.yaya_emoji02, R.drawable.yaya_emoji03, R.drawable.yaya_emoji04,
            R.drawable.yaya_emoji05, R.drawable.yaya_emoji06, R.drawable.yaya_emoji07, R.drawable.yaya_emoji08,
            R.drawable.yaya_emoji09, R.drawable.yaya_emoji10, R.drawable.yaya_emoji11, R.drawable.yaya_emoji12,
            R.drawable.yaya_emoji13, R.drawable.yaya_emoji14, R.drawable.yaya_emoji15, R.drawable.yaya_emoji16,
            R.drawable.yaya_emoji17, R.drawable.yaya_emoji18, R.drawable.yaya_emoji19
    };

    public int[] getResIdList() {
        return DEFAULT_EMO_RES_IDS;
    }

    public int[] getYayaResIdList() {
        return YAYA_EMO_RES_IDS;
    }

    public static synchronized Emoparser getInstance(Context cxt) {
        if (null == instance && null != cxt) {
            instance = new Emoparser(cxt);
        }
        return instance;
    }

    private Emoparser(Context cxt) {
        context = cxt;
        emoList = context.getResources().getStringArray(DEFAULT_SMILEY_TEXTS);
        yayaEmoList = context.getResources().getStringArray(YAYA_EMO_TEXTS);
        buildMap();
        buildYayaEmoMap();
        mPattern = buildPattern();
        mYayaPattern = buildYayaEmoPattern();
    }

    private void buildMap() {
        if (DEFAULT_EMO_RES_IDS.length != emoList.length) {
            throw new IllegalStateException("Smiley resource ID/text mismatch");
        }
        phraseIdMap = new HashMap<String, Integer>(emoList.length);
        idPhraseMap = new HashMap<Integer, String>(emoList.length);
        for (int i = 0; i < emoList.length; i++) {
            phraseIdMap.put(emoList[i], DEFAULT_EMO_RES_IDS[i]);
            idPhraseMap.put(DEFAULT_EMO_RES_IDS[i], emoList[i]);
        }
    }

    private void buildYayaEmoMap(){
        if (YAYA_EMO_RES_IDS.length != yayaEmoList.length) {
            throw new IllegalStateException("Yaya emo resource ID/text mismatch");
        }
        yayaPhraseIdMap = new HashMap<>(yayaEmoList.length);
        yayaIdPhraseMap = new HashMap<>(yayaEmoList.length);
        for (int i = 0; i < yayaEmoList.length; i++) {
            yayaPhraseIdMap.put(yayaEmoList[i], YAYA_EMO_RES_IDS[i]);
            yayaIdPhraseMap.put(YAYA_EMO_RES_IDS[i], yayaEmoList[i]);
        }
    }

    public HashMap<String, Integer> getPhraseIdMap() {
        return phraseIdMap;
    }

    public HashMap<Integer, String> getIdPhraseMap() {
        return idPhraseMap;
    }

    public HashMap<String, Integer> getYayaPhraseIdMap() {
        return yayaPhraseIdMap;
    }

    public HashMap<Integer, String> getYayaIdPhraseMap() {
        return yayaIdPhraseMap;
    }

    private Pattern buildPattern() {
        StringBuilder patternString = new StringBuilder(emoList.length * 3);
        patternString.append('(');
        for (String s : emoList) {
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
        patternString.replace(patternString.length() - 1,
                patternString.length(), ")");

        return Pattern.compile(patternString.toString());
    }

    private Pattern buildYayaEmoPattern() {
        StringBuilder patternString = new StringBuilder(yayaEmoList.length * 3);
        patternString.append('(');
        for (String s : yayaEmoList) {
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
        patternString.replace(patternString.length() - 1,
                patternString.length(), ")");

        return Pattern.compile(patternString.toString());
    }

    public CharSequence emoCharsequence(CharSequence text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        Matcher matcher = mPattern.matcher(text);
        while (matcher.find()) {
            int resId = phraseIdMap.get(matcher.group());
            Drawable drawable = context.getResources().getDrawable(resId);
            int size = (int) (CommonUtil.getElementSzie(context) * 0.8);
            drawable.setBounds(0, 0, size, size);
            ImageSpan imageSpan = new ImageSpan(drawable,
                    ImageSpan.ALIGN_BOTTOM);
            builder.setSpan(imageSpan, matcher.start(), matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        matcher = mYayaPattern.matcher(text);
        while (matcher.find()) {
            isGifEmo = true;
            int resId = yayaPhraseIdMap.get(matcher.group());
            Drawable drawable = context.getResources().getDrawable(resId);
            drawable.setBounds(0, 0, 105, 115);
            ImageSpan imageSpan = new ImageSpan(drawable,
                    ImageSpan.ALIGN_BOTTOM);
            builder.setSpan(imageSpan, matcher.start(), matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }

    public int getResIdByCharSequence(CharSequence text) {

        Matcher matcher = mYayaPattern.matcher(text);
        while (matcher.find()) {
            int resId = yayaPhraseIdMap.get(matcher.group());
            return resId;
        }
        return 0;
    }

    public boolean isMessageGif(CharSequence text){

        Matcher matcher = mYayaPattern.matcher(text);
        while (matcher.find()) {
            return true;
        }
        return false;
    }

}
