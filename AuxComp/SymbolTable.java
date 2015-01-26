package AuxComp;

import java.util.*;
import AST.NameAtom;

public class SymbolTable {

	public class KeyLevel{
		public String key;
		public int level;
		public KeyLevel(String key, int level){
			this.key=key;
			this.level=level;
		}
		@Override
	    public boolean equals(Object obj) {
	        if(obj != null && obj instanceof KeyLevel) {
	            KeyLevel s = (KeyLevel)obj;
	            return key.equals(s.key) && level==(s.level);
	        }
	        return false;
	    }

	    //Como key nunca começa com número, a combinação é única
	    @Override
	    public int hashCode() {
	        return (Integer.toString(level) + key).hashCode();
	    }
	}
		
	private HashMap<KeyLevel, Object> globalTable, localTable;

	public SymbolTable() {
		localTable  = new HashMap<KeyLevel, Object>();
		initialize();
	}

	public KeyLevel KeyLevel(String key, int level){
		return new KeyLevel(key,level);
	}

	public void remove(String key, int level){
		localTable.remove(KeyLevel(key, level));
	}

	public void put( String key, Object o, int level ) {
		localTable.put(KeyLevel(key,level), o);
	}
	
	public Object get( String key, int level ) {
		//procura do nivel mais alto até o mais baixo
		for(int i = level; i >=0; i--){
			if(localTable.get(KeyLevel(key,i))!=null){
				return localTable.get(KeyLevel(key,i));
			}
		}
		return null;
	}	

	public Object getLevel( String key, int level ) {
		return localTable.get(KeyLevel(key,level));
	}	
	

	public void removeIdent(int level) {
		//Se for -1 ele tira tudo
		if(level==-1){
			localTable.clear();
			initialize();
			return;
		}
		Iterator it = localTable.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        //REVER!!!
	        KeyLevel k = (KeyLevel)pairs.getKey();
	        if(k.level==level) it.remove();
	    }
	    initialize();
	}

	public void print(){
		Iterator it = localTable.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        //REVER!!!
	        KeyLevel k = (KeyLevel)pairs.getKey();
	        System.out.println("chave: "+k.key+" level: "+k.level);
	    }
	}

	public void initialize(){
		NameAtom a = new NameAtom("int", false);
		a.setType("int");
		put("int",a,0);
	}
}