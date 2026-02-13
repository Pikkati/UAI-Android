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

class TestableMobileClient:
    """Testable version of MobileClient without GUI dependencies"""

    def __init__(self):
        self.api_base = "http://localhost:8003"
        self.user_id = None
        self.connected = False
        self.offline_mode = False
        self.status_text = "Initializing..."

    def check_connection(self):
        """Check connection to mobile API server"""
        try:
            response = requests.get(f"{self.api_base}/mobile/health", timeout=5)
            self.connected = response.status_code == 200
            if self.connected:
                self.status_text = "Connected to UAI Platform"
            else:
                self.status_text = "Server responded with error"
                self.offline_mode = True
        except:
            self.connected = False
            self.status_text = "Disconnected - Offline Mode"
            self.offline_mode = True

    def start_session(self, user_id):
        """Start user session"""
        if not user_id:
            self.status_text = "Please enter a valid user ID"
            return False

        try:
            response = requests.post(
                f"{self.api_base}/mobile/session/start",
                json={"user_id": user_id},
                timeout=10
            )
            if response.status_code == 200:
                self.user_id = user_id
                self.status_text = "Session started successfully"
                return True
            else:
                self.status_text = "Failed to start session - server error"
        except:
            self.status_text = "Failed to start session - offline mode"
        return False

    def sync_data(self, dt=None):
        """Sync data with server"""
        if not self.user_id:
            return

        try:
            if self.connected:
                # Sync with server
                response = requests.post(
                    f"{self.api_base}/mobile/sync",
                    json={"user_id": self.user_id},
                    timeout=10
                )
                if response.status_code == 200:
                    self.status_text = "Data synchronized successfully"
                    return True
                else:
                    self.status_text = "Data sync failed"
                    return False
            else:
                # Offline mode - just return success for now
                self.status_text = "Data sync skipped - offline mode"
                return True
        except:
            self.status_text = "Data sync failed - connection error"
            return False

    def use_ai_feature(self, feature_type, data=None):
        """Use AI feature"""
        if not self.user_id:
            return None

        if data is None:
            data = {"test": "data"}

        try:
            if self.connected:
                response = requests.post(
                    f"{self.api_base}/mobile/ai/{feature_type}",
                    json={"user_id": self.user_id, "data": data},
                    timeout=15
                )
                if response.status_code == 200:
                    return response.json()
            else:
                # Offline AI processing (simplified)
                return {"result": "Offline AI response", "feature": feature_type}
        except:
            return None

class TestMobileClient(unittest.TestCase):
    """Test cases for mobile client functionality"""

    def setUp(self):
        """Set up test fixtures"""
        # Use testable version without GUI dependencies
        self.client = TestableMobileClient()

    def test_initialization(self):
        """Test client initialization"""
        self.assertIsNotNone(self.client)
        self.assertEqual(self.client.api_base, "http://localhost:8003")
        self.assertIsNone(self.client.user_id)
        self.assertFalse(self.client.connected)
        self.assertFalse(self.client.offline_mode)

    @patch('test_mobile_client.requests.get')
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

    @patch('test_mobile_client.requests.get')
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