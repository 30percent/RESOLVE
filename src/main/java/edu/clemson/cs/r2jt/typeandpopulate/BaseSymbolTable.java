package edu.clemson.cs.r2jt.typeandpopulate;

import edu.clemson.cs.r2jt.typeandpopulate.entry.SymbolTableEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>A helper class to factor out some logic repeated in 
 * <code>ScopeBuilder</code> and <code>Scope</code> and remove the temptation
 * to muck about with the entry map directly.</p>
 */
class BaseSymbolTable implements SymbolTable {

    private Map<String, SymbolTableEntry> myEntries = new HashMap<String, SymbolTableEntry>();
    private Map<Class<?>, List<SymbolTableEntry>> myEntriesByType =
            new HashMap<Class<?>, List<SymbolTableEntry>>();

    public BaseSymbolTable() {}

    public BaseSymbolTable(BaseSymbolTable source) {
        putAll(source.myEntries);
    }

    @Override
    public void put(String name, SymbolTableEntry entry) {
        myEntries.put(name, entry);

        boolean foundTopLevel = false;
        Class<?> entryClass = entry.getClass();

        while (!foundTopLevel) {
            foundTopLevel = entryClass.equals(SymbolTableEntry.class);

            List<SymbolTableEntry> classList = myEntriesByType.get(entryClass);
            if (classList == null) {
                classList = new LinkedList<SymbolTableEntry>();
                myEntriesByType.put(entryClass, classList);
            }

            classList.add(entry);

            entryClass = entryClass.getSuperclass();
        }
    }

    @Override
    public void putAll(Map<String, SymbolTableEntry> source) {
        for (Map.Entry<String, SymbolTableEntry> entry : source.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public SymbolTableEntry get(String name) {
        return myEntries.get(name);
    }

    @Override
    public boolean containsKey(String name) {
        return myEntries.containsKey(name);
    }

    @Override
    public Iterator<SymbolTableEntry> iterator() {
        return Collections.unmodifiableCollection(myEntries.values()).iterator();
    }

    @Override
    public <T extends SymbolTableEntry> Iterator<T> iterateByType(Class<T> type) {

        List<Class<T>> types = new LinkedList<Class<T>>();
        types.add(type);

        return iterateByType(types);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends SymbolTableEntry> Iterator<T> iterateByType(Collection<Class<T>> types) {
        List<T> result = new LinkedList<T>();

        List<T> typeList;
        for (Class<T> type : types) {
            typeList = (List<T>) myEntriesByType.get(type);

            if (typeList != null) {
                result.addAll(typeList);
            }
        }

        return Collections.unmodifiableList(result).iterator();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        boolean first = true;
        for (Map.Entry<String, SymbolTableEntry> entry : myEntries.entrySet()) {
            if (first) {
                first = false;
            }
            else {
                result.append(", ");
            }

            result.append(entry.getKey());
        }

        return result.toString();
    }
}
