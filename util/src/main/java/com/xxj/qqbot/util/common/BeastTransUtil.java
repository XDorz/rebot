package com.xxj.qqbot.util.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 兽音译者
 */
public class BeastTransUtil {

    private String[] lib=null;

    public static final String[] DEFAULT={"嗷","呜","啊","~"};

    private static final String[] HEX={"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f"};

    public static String encode(String context){
        BeastTransUtil transUtil=new BeastTransUtil(DEFAULT);
        return transUtil.toBeast(context);
    }

    public static String decode(String context){
        char[] lib=new char[4];
        lib[3]=context.charAt(0);
        lib[2]=context.charAt(context.length()-1);
        lib[1]=context.charAt(1);
        lib[0]=context.charAt(2);
        context=context.substring(3,context.length()-1);
        char[] chars = context.toCharArray();
        StringBuffer sb=new StringBuffer();
        int j=0;
        for (int i = 0; i < chars.length; i+=8) {
            int ch=0;
            for (int k = 0; k < 4; k++) {
                int n=(toInt(chars[i+2*k],lib)<<2|toInt(chars[i+2*k+1],lib))-j%16;
                if(n<0) n+=16;
                j++;
                ch=ch|n<<((3-k)*4);
            }
            sb.append((char)ch);
        }
        return sb.toString();
    }

    public static String encode(String context,String[] lib){
        if(lib==null){
            lib=DEFAULT;
        } else {
            if(lib.length>4){
                throw new MoriBotException("所给lib长度只能为4！！！");
            }
            Set<String> set = new HashSet<>(Arrays.asList(lib));
            if(set.size()!=4){
                throw new MoriBotException("所给lib不能含有相同的字符！！！");
            }
        }
        return new BeastTransUtil(lib).toBeast(context);
    }

    public String encodeBeast(String context){
        if(lib==null) lib=DEFAULT;
        return toBeast(context);
    }

    public BeastTransUtil(String[] lib) {
        this.lib = lib;
    }

    public BeastTransUtil(){ }

    public void setLib(String[] lib){
        this.lib = lib;
    }

    public String[] getLib(){
        return lib;
    }

    public static int toInt(char c,char[] lib){
        for (int i = 0; i < lib.length; i++) {
            if(lib[i]==c) return i;
        }
        throw new MoriBotException("所给兽语中含有字典中不存在的词，此兽语无法翻译！！！");
    }

    public String toBeast(String str){
        int i=0;
        StringBuffer sb=new StringBuffer();
        for (char c : str.toCharArray()) {
            int j=12;
            while (j>=0){
                int k=(c>>>j&0xf)+i%16;
                if(k>=16) k-=16;
                sb.append(lib[k>>>2&0b11]);
                sb.append(lib[k&0b11]);
                j-=4;
                i++;
            }
        }
        return lib[3]+lib[1]+lib[0]+sb.toString()+lib[2];
    }
}
