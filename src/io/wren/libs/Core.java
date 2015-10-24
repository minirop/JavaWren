package io.wren.libs;

public class Core {
	public static String source = "class Bool {}\r\n" + 
			"class Fiber {}\r\n" + 
			"class Fn {}\r\n" + 
			"class Null {}\r\n" + 
			"class Num {}\r\n" + 
			"\r\n" + 
			"class Sequence {\r\n" + 
			"  all(f) {\r\n" + 
			"    var result = true\r\n" + 
			"    for (element in this) {\r\n" + 
			"      result = f.call(element)\r\n" + 
			"      if (!result) return result\r\n" + 
			"    }\r\n" + 
			"    return result\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  any(f) {\r\n" + 
			"    var result = false\r\n" + 
			"    for (element in this) {\r\n" + 
			"      result = f.call(element)\r\n" + 
			"      if (result) return result\r\n" + 
			"    }\r\n" + 
			"    return result\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  contains(element) {\r\n" + 
			"    for (item in this) {\r\n" + 
			"      if (element == item) return true\r\n" + 
			"    }\r\n" + 
			"    return false\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  count {\r\n" + 
			"    var result = 0\r\n" + 
			"    for (element in this) {\r\n" + 
			"      result = result + 1\r\n" + 
			"    }\r\n" + 
			"    return result\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  count(f) {\r\n" + 
			"    var result = 0\r\n" + 
			"    for (element in this) {\r\n" + 
			"      if (f.call(element)) result = result + 1\r\n" + 
			"    }\r\n" + 
			"    return result\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  each(f) {\r\n" + 
			"    for (element in this) {\r\n" + 
			"      f.call(element)\r\n" + 
			"    }\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  isEmpty { iterate(null) ? false : true }\r\n" + 
			"\r\n" + 
			"  map(transformation) { MapSequence.new(this, transformation) }\r\n" + 
			"\r\n" + 
			"  where(predicate) { WhereSequence.new(this, predicate) }\r\n" + 
			"\r\n" + 
			"  reduce(acc, f) {\r\n" + 
			"    for (element in this) {\r\n" + 
			"      acc = f.call(acc, element)\r\n" + 
			"    }\r\n" + 
			"    return acc\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  reduce(f) {\r\n" + 
			"    var iter = iterate(null)\r\n" + 
			"    if (!iter) Fiber.abort(\"Can't reduce an empty sequence.\")\r\n" + 
			"\r\n" + 
			"    // Seed with the first element.\r\n" + 
			"    var result = iteratorValue(iter)\r\n" + 
			"    while (iter = iterate(iter)) {\r\n" + 
			"      result = f.call(result, iteratorValue(iter))\r\n" + 
			"    }\r\n" + 
			"\r\n" + 
			"    return result\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  join() { join(\"\") }\r\n" + 
			"\r\n" + 
			"  join(sep) {\r\n" + 
			"    var first = true\r\n" + 
			"    var result = \"\"\r\n" + 
			"\r\n" + 
			"    for (element in this) {\r\n" + 
			"      if (!first) result = result + sep\r\n" + 
			"      first = false\r\n" + 
			"      result = result + element.toString\r\n" + 
			"    }\r\n" + 
			"\r\n" + 
			"    return result\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  toList {\r\n" + 
			"    var result = List.new()\r\n" + 
			"    for (element in this) {\r\n" + 
			"      result.add(element)\r\n" + 
			"    }\r\n" + 
			"    return result\r\n" + 
			"  }\r\n" + 
			"}\r\n" + 
			"\r\n" + 
			"class MapSequence is Sequence {\r\n" + 
			"  construct new(sequence, fn) {\r\n" + 
			"    _sequence = sequence\r\n" + 
			"    _fn = fn\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  iterate(iterator) { _sequence.iterate(iterator) }\r\n" + 
			"  iteratorValue(iterator) { _fn.call(_sequence.iteratorValue(iterator)) }\r\n" + 
			"}\r\n" + 
			"\r\n" + 
			"class WhereSequence is Sequence {\r\n" + 
			"  construct new(sequence, fn) {\r\n" + 
			"    _sequence = sequence\r\n" + 
			"    _fn = fn\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  iterate(iterator) {\r\n" + 
			"    while (iterator = _sequence.iterate(iterator)) {\r\n" + 
			"      if (_fn.call(_sequence.iteratorValue(iterator))) break\r\n" + 
			"    }\r\n" + 
			"    return iterator\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  iteratorValue(iterator) { _sequence.iteratorValue(iterator) }\r\n" + 
			"}\r\n" + 
			"\r\n" + 
			"class String is Sequence {\r\n" + 
			"  bytes { StringByteSequence.new(this) }\r\n" + 
			"  codePoints { StringCodePointSequence.new(this) }\r\n" + 
			"}\r\n" + 
			"\r\n" + 
			"class StringByteSequence is Sequence {\r\n" + 
			"  construct new(string) {\r\n" + 
			"    _string = string\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  [index] { _string.byteAt_(index) }\r\n" + 
			"  iterate(iterator) { _string.iterateByte_(iterator) }\r\n" + 
			"  iteratorValue(iterator) { _string.byteAt_(iterator) }\r\n" + 
			"\r\n" + 
			"  count { _string.byteCount_ }\r\n" + 
			"}\r\n" + 
			"\r\n" + 
			"class StringCodePointSequence is Sequence {\r\n" + 
			"  construct new(string) {\r\n" + 
			"    _string = string\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  [index] { _string.codePointAt_(index) }\r\n" + 
			"  iterate(iterator) { _string.iterate(iterator) }\r\n" + 
			"  iteratorValue(iterator) { _string.codePointAt_(iterator) }\r\n" + 
			"\r\n" + 
			"  count { _string.count }\r\n" + 
			"}\r\n" + 
			"\r\n" + 
			"class List is Sequence {\r\n" + 
			"  addAll(other) {\r\n" + 
			"    for (element in other) {\r\n" + 
			"      add(element)\r\n" + 
			"    }\r\n" + 
			"    return other\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  toString { \"[\" + join(\", \") + \"]\" }\r\n" + 
			"\r\n" + 
			"  +(other) {\r\n" + 
			"    var result = this[0..-1]\r\n" + 
			"    for (element in other) {\r\n" + 
			"      result.add(element)\r\n" + 
			"    }\r\n" + 
			"    return result\r\n" + 
			"  }\r\n" + 
			"}\r\n" + 
			"\r\n" + 
			"class Map {\r\n" + 
			"  keys { MapKeySequence.new(this) }\r\n" + 
			"  values { MapValueSequence.new(this) }\r\n" + 
			"\r\n" + 
			"  toString {\r\n" + 
			"    var first = true\r\n" + 
			"    var result = \"{\"\r\n" + 
			"\r\n" + 
			"    for (key in keys) {\r\n" + 
			"      if (!first) result = result + \", \"\r\n" + 
			"      first = false\r\n" + 
			"      result = result + key.toString + \": \" + this[key].toString\r\n" + 
			"    }\r\n" + 
			"\r\n" + 
			"    return result + \"}\"\r\n" + 
			"  }\r\n" + 
			"}\r\n" + 
			"\r\n" + 
			"class MapKeySequence is Sequence {\r\n" + 
			"  construct new(map) {\r\n" + 
			"    _map = map\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  iterate(n) { _map.iterate_(n) }\r\n" + 
			"  iteratorValue(iterator) { _map.keyIteratorValue_(iterator) }\r\n" + 
			"}\r\n" + 
			"\r\n" + 
			"class MapValueSequence is Sequence {\r\n" + 
			"  construct new(map) {\r\n" + 
			"    _map = map\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  iterate(n) { _map.iterate_(n) }\r\n" + 
			"  iteratorValue(iterator) { _map.valueIteratorValue_(iterator) }\r\n" + 
			"}\r\n" + 
			"\r\n" + 
			"class Range is Sequence {}\r\n" + 
			"\r\n" + 
			"class System {\r\n" + 
			"  static print() {\r\n" + 
			"    writeString_(\"\\n\")\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  static print(obj) {\r\n" + 
			"    writeObject_(obj)\r\n" + 
			"    writeString_(\"\\n\")\r\n" + 
			"    return obj\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  static printAll(sequence) {\r\n" + 
			"    for (object in sequence) writeObject_(object)\r\n" + 
			"    writeString_(\"\\n\")\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  static write(obj) {\r\n" + 
			"    writeObject_(obj)\r\n" + 
			"    return obj\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  static writeAll(sequence) {\r\n" + 
			"    for (object in sequence) writeObject_(object)\r\n" + 
			"  }\r\n" + 
			"\r\n" + 
			"  static writeObject_(obj) {\r\n" + 
			"    var string = obj.toString\r\n" + 
			"    if (string is String) {\r\n" + 
			"      writeString_(string)\r\n" + 
			"    } else {\r\n" + 
			"      writeString_(\"[invalid toString]\")\r\n" + 
			"    }\r\n" + 
			"  }\r\n" + 
			"}\r\n" + 
			"";
}
