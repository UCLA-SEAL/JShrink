package edu.ucla.cs.jshrinklib.reachability;

import java.io.Serializable;

public class FieldData implements Serializable {
    private String name;
    private String className;
    private boolean isStatic;
    private String type;

    public FieldData(String name, String className, boolean isStatic, String type) {
        this.name = name;
        this.className = className;
        this.isStatic = isStatic;
        this.type = type;
    }

    public String getName(){
        return this.name;
    }

    public String getClassName(){
        return this.className;
    }

    public String getType(){
        return this.type;
    }

    public boolean isStatic(){
        return this.isStatic;
    }

    public void setClassName(String newClassName) {
        this.className = newClassName;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof FieldData){
            FieldData toCompare = (FieldData)o;
            if(this.name.equals(toCompare.name) && this.className.equals(toCompare.className)
                    && this.type.equals(toCompare.type) && this.isStatic == toCompare.isStatic){
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode(){
        int hash = this.name.hashCode() * 11 + 17;
        hash += this.className.hashCode() * 19;
        hash += this.type.hashCode() * 31;
        hash += this.isStatic ? 7 : 13;
        return hash;
    }

    @Override
    public String toString() {
        return (this.isStatic ? "static " : " ") + this.type + " " + this.name + "@" + this.className;
    }
}
