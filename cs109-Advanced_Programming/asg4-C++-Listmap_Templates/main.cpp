/* $Id: main.cpp,v 1.14 2011-03-01 18:12:10-08 dmfrank - $
 * Derek Frank, dmfank@ucsc.edu
 *
 * NAME
 *    mylist
 *
 * SYNOPSIS
 *    mylist [-u] [int|double|string]
 *
 * DESCRIPTION
 *    This program creates a list of TYPE specified by the user.  Input
 *    to be stored into the list is read from stdin.  Output writes to
 *    stdout.  Errors are written to stderr.  This program takes input,
 *    stores it in a list, sorts it in ascending order, and prints the
 *    output with one element per line.
 *
 * OPERANDS
 *    Operands specify the TYPE to be stored in the list.  Only a list
 *    of int, double, or string can be specified.  If no operand is
 *    given, the default of int will be assumed.
 *
 * OPTIONS
 *    The -u option sets the list to store only unique elements.  Any
 *    repeated elements are not stored.
 *
 * COMMANDS
 *    None.
 *
 * EXIT STATUS
 *
 *    0    No errors were detected.
 *
 *    1    Error messages were printed to the stderr.
 */

#include <cstdlib>
#include <iostream>
#include <istream>
#include <string>
#include <cstdio>
#include <unistd.h>

using namespace std;

#include "util.h"
#include "list.h"


//
// readlist
//    Reads from stdin and stores each element separated by a newline
//    into the given list reference.
//
template<typename T>
void readlist (list<T> &mylist, istream &stream, bool &isunique) {
   try {
      for (;;) {
         try {
            string line;
            getline (stream, line);
            if (stream.eof() ) {
               break;
            }
            T elem = from_string<T> (line);
            if (isunique == true) {
               if ( !mylist.contains_elem (elem) ) mylist.push (elem);
            } else {
               mylist.push (elem);
            }
         } catch (list_exn exn) {
            // If there is a problem discovered in any function, an
            // exn is thrown and printed here.
            complain() << exn.what() << endl;
         }
      }
   } catch (list_exit_exn) {
   }
}

//
// printlist
//    Prints a given list to cout with one element per line.
//
template<typename T>
void printlist (const list<T> &mylist) {
   cout << mylist;
}
      

//
// scan_options
//    Options analysis:  The only option is -Dflags. 
//
void scan_options (int argc, char **argv, bool &unique) {
   opterr = 0;
   for (;;) {
      int option = getopt (argc, argv, "u");
      if (option == EOF) break;
      switch (option) {
         case 'u':
            unique = true;
            break;
         default:
            complain() << "-" << (char) optopt << ": invalid option"
                       << endl;
            break;
      }
   }
   if ( (optind + 1) < argc ) {
      complain() << "operand not permitted" << endl;
   }
}

int main (int argc, char** argv) {
   sys_info::set_execname (argv[0]);
   bool unique = false;
   scan_options (argc, argv, unique);

   string intg = "int";
   string doub = "double";
   string str = "string";
   // list of ints, default list
   if (argv[optind] == NULL || argv[optind] == intg) {
      list<int> mylist;
      readlist<int> (mylist, cin, unique);
      mylist.sort();
      printlist<int> (mylist);
   }
   // list of doubles
   else if (argv[optind] == doub) {
      list<double> mylist;
      readlist<double> (mylist, cin, unique);
      mylist.sort();
      printlist<double> (mylist);
   }
   // list of strings
   else if (argv[optind] == str) {
      list<string> mylist;
      readlist<string> (mylist, cin, unique);
      mylist.sort();
      printlist<string> (mylist);
   }
   // Error for invalid operand
   else {
       complain() << "invalid operand:" << endl
                  << "mylist usage: mylist -{u} {int|double|string}"
                  << endl;
   }

   
   return sys_info::get_status();      
}
