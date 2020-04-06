package org.jackstaff.grpc.generator;

import java.util.Objects;

class SourceInfo {

    private String packageName;
    private String simpleName;

    public SourceInfo(Class<?> clz) {
        this(clz.getPackage().getName(), clz.getSimpleName());
    }

    public SourceInfo(String packageName, String simpleName) {
        this.packageName = packageName;
        this.simpleName = simpleName;
    }

    public String fullName(){
        return packageName+"."+ simpleName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceInfo that = (SourceInfo) o;
        return Objects.equals(packageName, that.packageName) &&
                Objects.equals(simpleName, that.simpleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName, simpleName);
    }

    @Override
    public String toString() {
        return "SourceInfo{" +
                "packageName='" + packageName + '\'' +
                ", name='" + simpleName + '\'' +
                '}';
    }
}
