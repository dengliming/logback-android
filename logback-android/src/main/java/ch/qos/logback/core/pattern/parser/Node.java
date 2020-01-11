/*
 * Copyright (c) 2020 Anthony Trinh.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.qos.logback.core.pattern.parser;

public class Node {
  static final int LITERAL = 0;
  static final int SIMPLE_KEYWORD = 1;
  static final int COMPOSITE_KEYWORD = 2;

  final int type;
  final Object value;
  Node next;

  Node(int type) {
    this(type, null);
  }

  Node(int type, Object value) {
    this.type = type;
    this.value = value;
  }

  /**
   * @return Returns the type.
   */
  public int getType() {
    return type;
  }

  /**
   * @return Returns the value.
   */
  public Object getValue() {
    return value;
  }

  public Node getNext() {
    return next;
  }

  public void setNext(Node next) {
    this.next = next;
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Node)) {
      return false;
    }
    Node r = (Node) o;

    return (type == r.type)
        && (value != null ? value.equals(r.value) : r.value == null)
        && (next != null ? next.equals(r.next) : r.next == null);
  }

  @Override
  public int hashCode() {
    int result = type;
    result = 31 * result + (value != null ? value.hashCode() : 0);
    return result;
  }

  String printNext() {
    if (next != null) {
      return " -> " + next;
    } else {
      return "";
    }
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    switch (type) {
    case LITERAL:
      buf.append("LITERAL(" + value + ")");
      break;
    default:
      buf.append(super.toString());
    }

    buf.append(printNext());
    
    return buf.toString();
  }
}