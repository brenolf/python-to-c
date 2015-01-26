package AuxComp;

import java.util.*;

public class TypeManager {

	public static String getFormatStr(String type){
		int index = getTypeId(type);

		if(index < 0)
			return "%p";

		return formt[index];
	}

	public static String getRawType(int type){
		return (rtype[type].equals("") ? "void" : rtype[type]);
	}

	public static String getRawType(String type){
		return getRawType(getTypeId(type));
	}

	public static String getCType(int type, String name){
		if(type < 0)
			return "struct class_" + type + "* " + name;

		return (ctype[type].equals("") ? "" : (ctype[type] + " ")) + name + (isList(type) ? "[]" : "");
	}

	public static String getCType(String type, String name){
		int index = getTypeId(type);

		if(index < 0)
			return "struct class_" + type + "* " + name;

		return getCType(index, name);
	}

	public static int getTypeId(String name){
		name = name.toUpperCase();

		for(int i = 0, l = title.length; i < l; i++){
			if(title[i].equals(name))
				return i;
		}

		return -1;
	}

	public static boolean isList(int type){
		return title[type].indexOf("_LIST") > -1;
	}
	
	public static boolean isList(String name){
		int index = getTypeId(name);
		return index >= 0 && isList(index);
	}

	public static String getListType(String name){
		String type = "";

		for(int i = 0; i < name.length(); i++){
			if(name.charAt(i) == '_')
				break;

			type += name.charAt(i);
		}

		return type;
	}

	public static final String title[] = {"VOID", "VOID*", "INT", "DOUBLE", "CHAR", "STRING", "STRING_LIST", "INT_LIST", "DOUBLE_LIST"};
	public static final String formt[] = {"", "%p", "%d", "%lf", "%c", "%s", "%p", "%p", "%p"};
	public static final String ctype[] = {"", "void*", "int", "double", "char", "char*", "char*", "int", "double"};
	public static final String rtype[] = {"void", "void", "int", "double", "char", "char", "char", "int", "double"};
}