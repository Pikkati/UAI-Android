#!/usr/bin/env python3
"""
Test script for UAI Android Mobile Client
Validates core functionality without GUI
"""

import sys
import os
import unittest
from unittest.mock import Mock, patch, MagicMock
import requests

# Mock kivy modules before any imports
sys.modules['kivy'] = MagicMock()
sys.modules['kivy.app'] = MagicMock()
sys.modules['kivy.uix'] = MagicMock()
sys.modules['kivy.uix.boxlayout'] = MagicMock()
sys.modules['kivy.uix.label'] = MagicMock()
sys.modules['kivy.uix.button'] = MagicMock()
sys.modules['kivy.uix.textinput'] = MagicMock()
sys.modules['kivy.uix.scrollview'] = MagicMock()
sys.modules['kivy.uix.gridlayout'] = MagicMock()
sys.modules['kivy.clock'] = MagicMock()
sys.modules['kivy.properties'] = MagicMock()

# Mock kivy classes with proper instantiation
class MockBoxLayout(MagicMock):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.orientation = kwargs.get('orientation', 'vertical')
        self.size_hint_y = kwargs.get('size_hint_y', 1)
        self.size_hint_x = kwargs.get('size_hint_x', 1)
        self.spacing = kwargs.get('spacing', 0)
        self.padding = kwargs.get('padding', 0)
        self.cols = kwargs.get('cols', 1)
        self.height = kwargs.get('height', None)
        self.children = []

    def add_widget(self, widget):
        self.children.append(widget)

    def bind(self, **kwargs):
        pass

class MockLabel(MagicMock):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.text = kwargs.get('text', '')
        self.size_hint_x = kwargs.get('size_hint_x', 1)
        self.size_hint_y = kwargs.get('size_hint_y', 1)
        self.bold = kwargs.get('bold', False)

class MockButton(MagicMock):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.text = kwargs.get('text', '')
        self.size_hint_y = kwargs.get('size_hint_y', 1)

    def bind(self, **kwargs):
        pass

class MockTextInput(MagicMock):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.hint_text = kwargs.get('hint_text', '')
        self.multiline = kwargs.get('multiline', False)
        self.size_hint_y = kwargs.get('size_hint_y', 1)

class MockScrollView(MagicMock):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.size_hint_y = kwargs.get('size_hint_y', 1)

    def add_widget(self, widget):
        pass

class MockGridLayout(MagicMock):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.cols = kwargs.get('cols', 1)
        self.spacing = kwargs.get('spacing', 0)
        self.padding = kwargs.get('padding', 0)
        self.size_hint_y = kwargs.get('size_hint_y', None)
        self.children = []

    def add_widget(self, widget):
        self.children.append(widget)

    def bind(self, **kwargs):
        pass

# Assign mock classes to modules
sys.modules['kivy.uix.boxlayout'].BoxLayout = MockBoxLayout
sys.modules['kivy.uix.label'].Label = MockLabel
sys.modules['kivy.uix.button'].Button = MockButton
sys.modules['kivy.uix.textinput'].TextInput = MockTextInput
sys.modules['kivy.uix.scrollview'].ScrollView = MockScrollView
sys.modules['kivy.uix.gridlayout'].GridLayout = MockGridLayout

# Mock Clock and properties
sys.modules['kivy.clock'].Clock = MagicMock()
sys.modules['kivy.clock'].Clock.schedule_interval = MagicMock()

sys.modules['kivy.properties'].StringProperty = MagicMock(return_value="test")
sys.modules['kivy.properties'].BooleanProperty = MagicMock(return_value=False)

# Now import the mobile client
from mobile_client import MobileClient, UAIAndroidApp

class TestMobileClient(unittest.TestCase):
    """Test cases for mobile client functionality"""

    def setUp(self):
        """Set up test fixtures"""
        # Mock the entire MobileClient to avoid GUI initialization
        with patch('mobile_client.MobileClient') as mock_client_class:
            mock_instance = MagicMock()
            mock_instance.api_base = "http://localhost:8003"
            mock_instance.user_id = None
            mock_instance.connected = False
            mock_instance.offline_mode = False
            mock_instance.status_text = "Initializing..."
            # Mock methods to avoid infinite loops and GUI operations
            mock_instance.check_connection = MagicMock()
            mock_instance.sync_data = MagicMock()
            mock_instance.build_ui = MagicMock()
            mock_client_class.return_value = mock_instance
            self.client = MobileClient()

        # Reset client state for each test
        self.client.api_base = "http://localhost:8003"
        self.client.user_id = None
        self.client.connected = False
        self.client.offline_mode = False
        self.client.status_text = "Initializing..."

    def test_initialization(self):
        """Test client initialization"""
        self.assertIsNotNone(self.client)
        self.assertEqual(self.client.api_base, "http://localhost:8003")
        self.assertIsNone(self.client.user_id)
        self.assertFalse(self.client.connected)
        self.assertFalse(self.client.offline_mode)

    @patch('requests.get')
    def test_connection_check_success(self, mock_get):
        """Test successful connection check"""
        mock_response = Mock()
        mock_response.status_code = 200
        mock_get.return_value = mock_response

        # Simulate connection check
        self.client.check_connection()

        # Note: In real implementation, this runs in a thread
        # For testing, we call it directly
        self.assertTrue(self.client.connected)
        self.assertIn("Connected", self.client.status_text)

    @patch('requests.get')
    def test_connection_check_failure(self, mock_get):
        """Test failed connection check"""
        mock_get.side_effect = requests.exceptions.RequestException()

        self.client.check_connection()

        self.assertFalse(self.client.connected)
        self.assertTrue(self.client.offline_mode)
        self.assertIn("Disconnected", self.client.status_text)

    @patch('requests.post')
    def test_start_session_success(self, mock_post):
        """Test successful session start"""
        mock_response = Mock()
        mock_response.status_code = 200
        mock_post.return_value = mock_response

        self.client.start_session("test_user")

        self.assertEqual(self.client.user_id, "test_user")
        self.assertIn("Session started", self.client.status_text)

    @patch('requests.post')
    def test_start_session_failure(self, mock_post):
        """Test failed session start"""
        mock_post.side_effect = requests.exceptions.RequestException()

        self.client.start_session("test_user")

        self.assertIn("Offline", self.client.status_text)
        self.assertTrue(self.client.offline_mode)

    def test_start_session_empty_user(self):
        """Test session start with empty user ID"""
        self.client.start_session("")

        self.assertIn("Please enter", self.client.status_text)
        self.assertIsNone(self.client.user_id)

    @patch('requests.post')
    def test_use_ai_feature_with_session(self, mock_post):
        """Test AI feature usage with active session"""
        mock_response = Mock()
        mock_response.status_code = 200
        mock_post.return_value = mock_response

        self.client.user_id = "test_user"
        self.client.use_ai_feature("predictive_analytics")

        self.assertIn("AI Feature used", self.client.status_text)

    def test_use_ai_feature_no_session(self):
        """Test AI feature usage without session"""
        self.client.use_ai_feature("predictive_analytics")

        self.assertIn("Please start a session", self.client.status_text)

    @patch('requests.post')
    def test_sync_data_connected(self, mock_post):
        """Test data sync when connected"""
        mock_response = Mock()
        mock_response.status_code = 200
        mock_post.return_value = mock_response

        self.client.connected = True
        self.client.user_id = "test_user"
        self.client.sync_data()

        self.assertIn("Data synchronized", self.client.status_text)

    def test_sync_data_disconnected(self):
        """Test data sync when disconnected"""
        self.client.connected = False
        self.client.sync_data()

        # Should not attempt sync when disconnected
        self.assertNotIn("Data synchronized", self.client.status_text)


class TestUAIAndroidApp(unittest.TestCase):
    """Test cases for the main app class"""

    def setUp(self):
        """Set up test fixtures"""
        self.app = UAIAndroidApp(demo_mode=True)

    def test_app_initialization(self):
        """Test app initialization"""
        self.assertIsNotNone(self.app)
        self.assertTrue(self.app.demo_mode)


def run_tests():
    """Run all tests"""
    print("🧪 Running UAI Android Mobile Client Tests...")

    # Create test suite
    loader = unittest.TestLoader()
    suite = unittest.TestSuite()

    # Add test cases
    suite.addTests(loader.loadTestsFromTestCase(TestMobileClient))
    suite.addTests(loader.loadTestsFromTestCase(TestUAIAndroidApp))

    # Run tests
    runner = unittest.TextTestRunner(verbosity=2)
    result = runner.run(suite)

    # Report results
    if result.wasSuccessful():
        print("✅ All tests passed!")
        return True
    else:
        print(f"❌ {len(result.failures)} failures, {len(result.errors)} errors")
        return False


if __name__ == '__main__':
    success = run_tests()
    sys.exit(0 if success else 1)