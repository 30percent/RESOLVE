package edu.clemson.cs.r2jt.proving.immutableadts;

import java.util.Iterator;

public class ImmutableListSubview<E> extends AbstractImmutableList<E> {

    private final ArrayBackedImmutableList<E> myBaseList;
    private final int mySubviewStart;
    private final int mySubviewLength;
    private final int myFirstAfterIndex;

    public ImmutableListSubview(ArrayBackedImmutableList<E> baseList, int start, int length) {

        //TODO : These defensive checks can be taken out for efficiency once
        //       we're satisfied that ImmutableLists works correctly.
        if (start + length > baseList.size()) {
            throw new IllegalArgumentException("View exceeds source bounds.");
        }

        if (length < 0) {
            throw new IllegalArgumentException("Negative length.");
        }

        if (start < 0) {
            throw new IllegalArgumentException("Negative start.");
        }

        myBaseList = baseList;
        mySubviewStart = start;
        mySubviewLength = length;
        myFirstAfterIndex = mySubviewStart + mySubviewLength;
    }

    @Override
    public E get(int index) {
        if (index < 0 || index >= myFirstAfterIndex) {
            throw new IndexOutOfBoundsException();
        }

        return myBaseList.get(index + mySubviewStart);
    }

    @Override
    public ImmutableList<E> head(int length) {
        if (length > mySubviewLength) {
            throw new IndexOutOfBoundsException();
        }

        return new ImmutableListSubview<E>(myBaseList, mySubviewStart, length);
    }

    @Override
    public Iterator<E> iterator() {
        return myBaseList.subsequenceIterator(mySubviewStart, mySubviewLength);
    }

    @Override
    public int size() {
        return mySubviewLength;
    }

    @Override
    public ImmutableList<E> tail(int startIndex) {
        if (startIndex < 0 || startIndex > mySubviewLength) {
            throw new IndexOutOfBoundsException();
        }

        return new ImmutableListSubview<E>(myBaseList, startIndex + mySubviewStart, mySubviewLength
                - startIndex);
    }

}
