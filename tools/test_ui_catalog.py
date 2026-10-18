import unittest
from unittest.mock import patch
import io,json
from extract_ui_catalog import literals
from localize import protect,restore,validate,translate_many

class UiCatalogTests(unittest.TestCase):
 def test_nested_kotlin_interpolation_and_quoted_branches(self):
  values=literals('val title="${if(done)"Complete" else "Continue"}: $count of ${p.answers.size}"')
  templates=[item[1] for item in values]
  self.assertIn('Complete',templates)
  self.assertIn('Continue',templates)
  self.assertIn('{0}: {1} of {2}',templates)
 def test_escaped_newline_comments_and_character_literals(self):
  values=literals('// "ignore"\nval c=\'"\'; val s="A\\nB" /* "ignore" */')
  self.assertEqual(['A\nB'],[item[1] for item in values])
 def test_dynamic_parameters_survive_provider_roundtrip(self):
  text='Found {0} of {1}: {2}'
  marked,values=protect(text)
  self.assertEqual(text,restore(marked,values))
  with self.assertRaises(ValueError):validate(text,'Trouvé {0} sur {1}')
 def test_provider_batch_preserves_order_and_parameters(self):
  def response(request,timeout):
   payload=json.loads(request.data)
   self.assertEqual(['EN','DE'],[payload['source_lang'],payload['target_lang']])
   self.assertEqual('https://api-free.deepl.com/v2/translate',request.full_url)
   return io.BytesIO(json.dumps({'translations':[{'text':s} for s in payload['text']]}).encode())
  with patch('urllib.request.urlopen',side_effect=response):
   self.assertEqual(['Found {0}','Price %1$s'],translate_many(['Found {0}','Price %1$s'],'de','test-only:fx'))
 def test_expanded_xml_placeholder_form_is_preserved(self):
  self.assertEqual('Found {0}',restore('Found <x id="0"></x>',['{0}']))

if __name__=='__main__':unittest.main()
