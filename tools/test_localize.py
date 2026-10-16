import unittest
from localize import protect, restore, validate
class LocalisationTests(unittest.TestCase):
 def test_roundtrip(self):
  s='Hello %1$s, {count} flowers and %d hints';p,v=protect(s);self.assertEqual(s,restore(p,v))
 def test_mismatch_rejected(self):
  with self.assertRaises(ValueError):validate('%1$s blooms','blooms')
 def test_duplicate_rejected(self):
  with self.assertRaises(ValueError):restore('<x id="0"/><x id="0"/>',['%s'])
 def test_empty_rejected(self):
  with self.assertRaises(ValueError):validate('Hello','')
if __name__=='__main__':unittest.main()
