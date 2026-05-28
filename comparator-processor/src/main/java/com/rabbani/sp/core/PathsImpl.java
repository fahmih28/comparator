package com.rabbani.sp.core;

import com.rabbani.sp.core.path.*;

class PathsImpl {

     static class FieldPathImpl implements FieldPath {

         final String name;

         final Path parent;

         final String relativePath;

         final String canonicalPath;

         public FieldPathImpl(String name,Path parent) {
             this.name = name;
             this.parent = parent;
             this.relativePath = name;
             this.canonicalPath = parent.canonicalPath()+"."+relativePath;
         }

         @Override
         public String name() {
             return name;
         }

         @Override
         public Path parent() {
             return parent;
         }

         @Override
         public String relativePath() {
             return relativePath;
         }

         @Override
         public String canonicalPath() {
             return canonicalPath;
         }
     }

    static class KeyPathImpl implements KeyPath {

        final String key;

        final Path parent;

        final String relativePath;

        final String canonicalPath;

        public KeyPathImpl(String key,Path parent) {
            this.key = key;
            this.parent = parent;
            this.relativePath = key;
            this.canonicalPath = parent.canonicalPath()+"."+key;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public Path parent() {
            return parent;
        }

        @Override
        public String relativePath() {
            return "";
        }

        @Override
        public String canonicalPath() {
            return canonicalPath;
        }
    }

    static class ArrayPathImpl implements ArrayPath {

        final int index;

        final Path parent;

        final String relativePath;

        final String canonicalPath;

        public ArrayPathImpl(int index,Path parent) {
            this.index = index;
            this.parent = parent;
            this.relativePath = "["+index+"]";
            this.canonicalPath = parent.canonicalPath()+relativePath;
        }

        @Override
        public int index() {
            return index;
        }

        @Override
        public Path parent() {
            return parent;
        }

        @Override
        public String relativePath() {
            return relativePath;
        }

        @Override
        public String canonicalPath() {
            return canonicalPath;
        }
    }

    static class CollectionPathImpl implements CollectionPath {

        final int index;

        final Path parent;

        final String relativePath;

        final String canonicalPath;

        public CollectionPathImpl(int index,Path parent) {
            this.index = index;
            this.parent = parent;
            this.relativePath = "["+index+"]";
            this.canonicalPath = parent.canonicalPath()+relativePath;
        }

        @Override
        public int index() {
            return index;
        }

        @Override
        public Path parent() {
            return parent;
        }

        @Override
        public String relativePath() {
            return "";
        }

        @Override
        public String canonicalPath() {
            return canonicalPath;
        }
    }

    static class RootPathImpl implements RootPath {

        @Override
        public Path parent() {
            return null;
        }

        @Override
        public String relativePath() {
            return "$";
        }

        @Override
        public String canonicalPath() {
            return "$";
        }
    }
}
