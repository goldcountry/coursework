/* $Id: Matrix.java,v 1.2 2010-10-31 07:26:38-07 dmfrank - $
 * Derek Frank, dmfrank@ucsc.edu
 * 
 * NAME
 *   Matrix -- a n*n sparse matrix of double values.
 * 
 * DESCRIPTION
 *   A class representing a sparse (the number of non-zero
 * entries is small compared to the total number of entries) n*n
 * matrix of double values.  Contains a List array that holds
 * Objects.  The Object specified by Matrix to be contained in a
 * List is a private inner class, Entry, which contains the
 * column entry along with its corresponding double value.  This
 * class is capable of performing specified Matrix operations
 * that exploits the (expected) sparseness of it's Matrix
 * operands.
 */

class Matrix {
  
  // Inner Classes //////////////////////////////////////////////
  
  /* class Entry
   * Private inner class.
   */
  private class Entry {
    
    // Fields //
    int col;
    double val;
    
    // Constructor //
    
    /* Entry()
     * Creates a new Entry.
     */
    Entry(int col, double val) {
      this.col = col;
      this.val = val;
    }
    
    // Access Functions //
    
    /* equals()
     * Overrides Object's equals() method.
     * Pre: (obj instanceof Entry)
     */
    public boolean equals(Object obj) {
      if ( !(obj instanceof Entry) ) return false;
      if (obj == this) return true;
      Entry E = (Entry)obj;
      
      if (this.col == E.col && this.val == E.val) return true;
      return false;
    }
    
    // Other Functions //
    
    /* toString()
     * Overrides Object's toString() method.
     */
    public String toString() {
      String str = "(";
      str += String.valueOf(this.col) + ", ";
      str += String.valueOf(this.val) + ")";
      return str;
    }
  }
  
  // Fields /////////////////////////////////////////////////////
  private List[] row;
  private int size;
  private int NNZ;
  
  // Constructor ////////////////////////////////////////////////
  
  /* Matrix()
   * Constructor that makes a new n*n zero Matrix.
   * Pre: n >= 1
   */
  Matrix(int n) {
    if ( n<1 ) {
      throw new RuntimeException("Matrix Error: Matrix() called on invalid n*n Matrix");
    }
    size = n;
    NNZ = 0;
    row = new List[size];
    for (int i=0; i<size; ++i)
      row[i] = new List();
  }
  
  // Access Functions ///////////////////////////////////////////
  
  /* getSize()
   * Returns n, the number of rows and columns of this Matrix.
   */
  int getSize() {
    return size;
  }
  
  /* getNNZ()
   * Returns the number of non-zero entries in this Matrix.
   */
  int getNNZ() {
    return NNZ;
  }
  
  /* equals()
   * Overrides Object's equals() method.
   * Pre: (obj instanceof Matrix)
   */
  public boolean equals(Object obj) {
    if ( !(obj instanceof Matrix) ) return false;
    if (obj == this) return true;
    Matrix M = (Matrix)obj;
    
    boolean flag = true;
    List list1;
    List list2;
    if (this.size == M.size && this.NNZ == M.NNZ) {
      int i = 0;
      list1 = this.row[i];
      while ( flag && list1 != null && i < size ) {
        list1 = this.row[i];
        list2 = M.row[i];
        flag = list1.equals(list2);
        ++i;
      }
      return flag;
    }
    return false;
  }
  
  // Maipulation Procedures /////////////////////////////////////
  
  /* makeZero()
   * Sets this Matrix to the zero state.
   */
  void makeZero() {
    // Step through the rows and make them empty
    for (int i=0; i<size; ++i) {
      row[i].makeEmpty();
    }
    // All non-zero entries removed
    NNZ = 0;
  }
  
  /* copy()
   * Returns a new Matrix having the same entries as this Matrix.
   */
  Matrix copy() {
    Matrix copy = new Matrix(size);
    List R;
    Entry E;
    // Step through the rows
    for (int i=0; i<size; ++i) {
      R = row[i];
      if ( !R.isEmpty() ) {
        // Set row[i] current to first entry
        R.moveFirst();
        // Step through the entries and insert into the current row
        while ( !R.offEnd() ) {
          assert ( R.getCurrent() instanceof Entry ):"Matrix Error: copy() Object is not an instance of Entry";
          E = (Entry)R.getCurrent();
          E = new Entry(E.col,E.val);
          copy.row[i].insertAfterLast(E);
          R.moveNext();
        }
      }
    }
    copy.NNZ = NNZ; // Have same non-zero numbers.
    return copy;
  }
  
  /* changeEntry()
   * Changes the ith row, jth column of this Matrix to x.
   * Pre: 1 <= i <= getSize(); 1 <= j <= getSize()
   */
  void changeEntry(int i, int j, double x) {
    if ( i<1 || j<1 || j>size ) {
      throw new RuntimeException("Matrix Error: changeEntry() called on invalid Matrix entry");
    }
    
    boolean found = false;
    Entry newE = new Entry(j,x);
    Entry currE;
    List R = row[i-1]; // Array is offset by 1 from user.
    // Current row is empty. Just insert new Entry.
    if ( R.isEmpty() && x!=0 ) {
      R.insertAfterLast(newE);
      ++NNZ;
    }
    // Column is in first half of n.
    // Start current from first Entry.
    else {
      if ( j < size/2 ) {
        R.moveFirst();
        while ( !found ) {
          // Postcondition assertion.
          assert ( R.getCurrent() instanceof Entry ):"Matrix Error: changeEntry() Object is not an instance of Entry";
          currE = (Entry)R.getCurrent();
          
          // If-else statements specify when to insert Entry.
          if ( currE.col == j ) {
            // Change the entry if non-zero. Otherwise delete entry.
            if ( x!=0 ) { R.insertAfterCurrent(newE); }
            else { --NNZ; }
            R.deleteCurrent();
            found = true;
          }else if ( currE.col > j ) {
            // Insert the entry if non-zero and current entry is zero.
            if ( x!=0 ) {
              R.insertBeforeCurrent(newE);
              ++NNZ;
            }
            found = true;
          }else if ( R.atLast() ) {
            // Insert the entry if non-zero and at last entry.
            if ( x!=0 ) {
              R.insertAfterCurrent(newE);
              ++NNZ;
            }
            found = true;
          }
          R.moveNext();
        }
      }
      // Column is in second half of n.
      // Start current from last Entry.
      else {
        R.moveLast();
        while ( !found ) {
          // Postcondition assertion.
          assert ( R.getCurrent() instanceof Entry ):"Matrix Error: changeEntry() Object is not an instance of Entry";;
          currE = (Entry)R.getCurrent();
          
          // If-else statements specify when to insert Entry.
          if ( currE.col == j ) {
            // Change the entry if non-zero. Otherwise delete entry.
            if ( x!=0 ) { R.insertBeforeCurrent(newE); }
            else { --NNZ; }
            R.deleteCurrent();
            found = true;
          }else if ( currE.col < j ) {
            // Insert the entry if non-zero and current entry is zero.
            if ( x!=0 ) {
              R.insertAfterCurrent(newE);
              ++NNZ;
            }
            found = true;
          }else if ( R.atFirst() ) {
            // Insert the entry if non-zero and at first entry.
            if ( x!=0 ) {
              R.insertBeforeCurrent(newE);
              ++NNZ;
            }
            found = true;
          }
          R.movePrev();
        }
      }
    }
  }
  
  /* scalarMult()
   * Returns a new Matrix that is the scalar product of this
   * Matrix with x.
   */
  Matrix scalarMult(double x) {
    Matrix newM = new Matrix(size);
    List R;
    Entry E;
    
    // Step through the rows.
    for (int i=0; i<size; ++i) {
      R = row[i];
      if ( !R.isEmpty() ) {
        R.moveFirst();
        // Step through the entries.
        while ( !R.offEnd() ) {
          assert ( R.getCurrent() instanceof Entry ):"Matrix Error: scalarMult() Object is not an instance of Entry";
          E = (Entry)R.getCurrent();
          E = new Entry(E.col,E.val);
          // Multiply value by scalar.
          E.val = x*E.val;
          // Insert into new Matrix.
          newM.row[i].insertAfterLast(E);
          R.moveNext();
        }
      }
    }
    newM.NNZ = NNZ; // Have same non-zero numbers.
    return newM;
  }
  
  /* add()
   * Returns a new Matrix that is the sum of this Matrix with M.
   * Pre: getSize() == M.getSize()
   */
  Matrix add(Matrix M) {
    if ( size != M.size ) {
      throw new RuntimeException("Matrix Error: add() called on Matrices of nonequal sizes");
    }
    
    // If this and M reference the same Matrix then duplicate.
    if ( this == M ) {
      M = this.copy();
    }
    
    Matrix newM = new Matrix(size);
    List R1, R2;
    Entry E, E2;
    
    for (int i=0; i<size; ++i) {
      R1 = row[i];
      R2 = M.row[i];
      
      // Check if both rows have non-zero entries.
      if ( !R1.isEmpty() ) R1.moveFirst();
      if ( !R2.isEmpty() ) R2.moveFirst();
      while ( !R1.offEnd() || !R2.offEnd() ) {
        // The current Entry of R1 has run off the end.
        // Just add the rest of R2.
        if ( R1.offEnd() ) {
          assert ( R2.getCurrent() instanceof Entry ):"Matrix Error: add() Object is not an instance of Entry";
          E2 = (Entry)R2.getCurrent();
          E2 = new Entry(E2.col,E2.val);
          // Insert current R2 Entry into newM.
          newM.row[i].insertAfterLast(E2);
          ++newM.NNZ;
          R2.moveNext();
        }
        // The current Entry of R2 has run off the end.
        // Just add the rest of R1.
        else if ( R2.offEnd() ) {
          assert ( R1.getCurrent() instanceof Entry ):"Matrix Error: add() Object is not an instance of Entry";
          E = (Entry)R1.getCurrent();
          E = new Entry(E.col,E.val);
          // Insert current R2 Entry into newM.
          newM.row[i].insertAfterLast(E);
          ++newM.NNZ;
          R1.moveNext();
        }
        // Neither current Entry of R1 or R2 has run off end.
        // Add the corresponding Entries.
        else {
          assert ( R1.getCurrent() instanceof Entry ):"Matrix Error: add() Object is not an instance of Entry";
          assert ( R2.getCurrent() instanceof Entry ):"Matrix Error: add() Object is not an instance of Entry";
          E = (Entry)R1.getCurrent();
          E2 = (Entry)R2.getCurrent();
          
          // The Entries are in the same column.
          if ( E.col == E2.col ) {
            E = new Entry(E.col,E.val);
            E.val = E.val + E2.val;
            // If sum is zero do not insert Entry.
            if ( E.val != 0 ) {
              newM.row[i].insertAfterLast(E);
              ++newM.NNZ;
            }
            R1.moveNext();
            R2.moveNext();
          }
          // The current Entry in R1 is in a lower column than R2
          else if ( E.col < E2.col ) {
            E = new Entry(E.col,E.val);
            newM.row[i].insertAfterLast(E);
            ++newM.NNZ;
            R1.moveNext();
          }
          // The current Entry in R2 is in a lower column than R1
          else {
            E2 = new Entry(E2.col,E2.val);
            newM.row[i].insertAfterLast(E2);
            ++newM.NNZ;
            R2.moveNext();
          }
        }
      }
    }
    return newM;
  }
  
  /* sub()
   * Returns a new Matrix that is the difference of this Matrix
   * with M.
   * Pre: getSize() == M.getSize()
   */
  Matrix sub(Matrix M) {
    if ( size != M.size ) {
      throw new RuntimeException("Matrix Error: sub() called on Matrices of nonequal sizes");
    }
    
    // If this and M reference the same Matrix then duplicate.
    if ( this == M ) {
      M = this.copy();
    }
    
    Matrix newM = new Matrix(size);
    List R1, R2;
    Entry E, E2;
    
    for (int i=0; i<size; ++i) {
      R1 = row[i];
      R2 = M.row[i];
      
      // Check if both rows have non-zero entries.
      if ( !R1.isEmpty() ) R1.moveFirst();
      if ( !R2.isEmpty() ) R2.moveFirst();
      while ( !R1.offEnd() || !R2.offEnd() ) {
        // The current Entry of R1 has run off the end.
        // Just subtract the rest of R2.
        if ( R1.offEnd() ) {
          assert ( R2.getCurrent() instanceof Entry ):"Matrix Error: sub() Object is not an instance of Entry";
          E2 = (Entry)R2.getCurrent();
          E2 = new Entry(E2.col,E2.val);
          // Subtract value from zero
          E2.val = -E2.val;
          // Insert current R2 Entry into newM.
          newM.row[i].insertAfterLast(E2);
          ++newM.NNZ;
          R2.moveNext();
        }
        // The current Entry of R2 has run off the end.
        // Just add the rest of R1.
        else if ( R2.offEnd() ) {
          assert ( R1.getCurrent() instanceof Entry ):"Matrix Error: sub() Object is not an instance of Entry";
          E = (Entry)R1.getCurrent();
          E = new Entry(E.col,E.val);
          // Insert current R2 Entry into newM.
          newM.row[i].insertAfterLast(E);
          ++newM.NNZ;
          R1.moveNext();
        }
        // Neither current Entry of R1 or R2 has run off end.
        // Subtract corresponding R2 Entry from R1 Entry.
        else {
          assert ( R1.getCurrent() instanceof Entry ):"Matrix Error: sub() Object is not an instance of Entry";
          assert ( R2.getCurrent() instanceof Entry ):"Matrix Error: sub() Object is not an instance of Entry";
          E = (Entry)R1.getCurrent();
          E2 = (Entry)R2.getCurrent();
          
          // The Entries are in the same column.
          if ( E.col == E2.col ) {
            E = new Entry(E.col,E.val);
            E.val = E.val - E2.val;
            // If sum is zero do not insert Entry.
            if ( E.val != 0 ) {
              // Insert difference into newM.
              newM.row[i].insertAfterLast(E);
              ++newM.NNZ;
            }
            R1.moveNext();
            R2.moveNext();
          }
          // The current Entry in R1 is in a lower column than R2
          // Just add R1 Entry to zero.
          else if ( E.col < E2.col ) {
            E = new Entry(E.col,E.val);
            // Insert difference into newM.
            newM.row[i].insertAfterLast(E);
            ++newM.NNZ;
            R1.moveNext();
          }
          // The current Entry in R2 is in a lower column than R1
          // Subtract R2 Entry from zero.
          else {
            E2 = new Entry(E2.col,E2.val);
            // Subtract value from zero.
            E2.val = -E2.val;
            // Insert difference into newM.
            newM.row[i].insertAfterLast(E2);
            ++newM.NNZ;
            R2.moveNext();
          }
        }
      }
    }
    return newM;
  }
  
  /* transpose()
   * Returns a new Matrix that is the transpose of this Matrix.
   */
  Matrix transpose() {
    Matrix newM = new Matrix(size);
    List R;
    Entry E;
    int j;
    
    // Step through the rows
    for (int i=0; i<size; ++i) {
      R = row[i];
      if ( !R.isEmpty() ) {
        // Set row[i] current to first entry
        R.moveFirst();
        // Step through the entries and insert into the current row
        while ( !R.offEnd() ) {
          assert ( R.getCurrent() instanceof Entry ):"Matrix Error: copy() Object is not an instance of Entry";
          E = (Entry)R.getCurrent();
          // New row will be current column.
          j = E.col;
          E = new Entry(i+1,E.val);
          // Insert Entry into newM.
          newM.row[j-1].insertAfterLast(E);
          R.moveNext();
        }
      }
    }
    newM.NNZ = NNZ; // Have same non-zero numbers.
    return newM;
  }
  
  /* mult()
   * Returns a new Matrix that is the product of this Matrix with
   * M.
   * Pre: getSize() == M.getSize()
   */
  Matrix mult(Matrix M) {
    if ( size != M.size ) {
      throw new RuntimeException("Matrix Error: mult() called on matrices of nonequal sizes");
    }
    
    // If this and M reference the same Matrix then duplicate.
    if ( this == M ) {
      M = this.copy();
    }
    
    Matrix newM = new Matrix(size);
    List R, C;
    Entry E;
    double x;
    
    // Transposing M makes it easier to take the dot product.
    Matrix transposeM = M.transpose();
    // Step through newM and insert each vector dot product.
    for (int i=0; i<size; ++i) {
      R = row[i];
      for (int j=0; j<size; ++j) {
        C = transposeM.row[j];
        x = dot(R,C);
        if ( x!=0 ) {
          E = new Entry(j+1,x);
          newM.row[i].insertAfterLast(E);
          ++newM.NNZ;
        }
      }
    }
    return newM;
  }
  
  /* dot()
   * Private function that returns the vector dot product of a
   * Matrix row and column.
   */
  private static double dot(List R, List C) {
    double dot = 0;
    // If row or column is all zeroes then dot product is zero.
    // When a row or column is not all zeroes then:
    if ( !R.isEmpty() && !C.isEmpty() ) {
      Entry E, E2;
      R.moveFirst();
      C.moveFirst();
      
      while ( !R.offEnd() && !C.offEnd() ) {
        assert ( R.getCurrent() instanceof Entry ):"Matrix Error: dot() Object is not an instance of Entry";
        assert ( C.getCurrent() instanceof Entry ):"Matrix Error: dot() Object is not an instance of Entry";
        E = (Entry)R.getCurrent();
        E2 = (Entry)C.getCurrent();
        
        // The Entries are in the same column.
        if ( E.col == E2.col ) {
          dot += E.val*E2.val;
          R.moveNext();
          C.moveNext();
        }
        // The current Entry in R is in a lower column than C.
        // Multiply R Entry by zero.
        else if ( E.col < E2.col ) {
          // Value is zero.
          R.moveNext();
        }
        // The current Entry in C is in a lower column than R
        // Multiply C Entry by zero.
        else {
          C.moveNext();
        }
      }
    }
    return dot;
  }

  // Other Functions ////////////////////////////////////////////
  
  /* toString()
   * Overrides Object's toString() method.
   */
  public String toString() {
    String str = "";
    for (int i=1; i<=size; ++i) {
      if ( row[i-1].getLength() != 0 ) {
        str += String.valueOf(i) + ": ";
        str += row[i-1].toString() + "\n";
      }
    }
    return str;
  }
  
}