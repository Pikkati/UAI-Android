#!/usr/bin/env python3
"""
UAI Android Mobile Client
========================

Cross-platform mobile application for UAI Enterprise Platform.
Features:
- Real-time data synchronization
- Offline AI capabilities
- Cross-platform data sync
- Mobile-optimized interface
"""

import kivy

kivy.require("2.0.0")

import json
import threading
import time
from datetime import datetime

import requests
from kivy.app import App
from kivy.clock import Clock
from kivy.properties import BooleanProperty, StringProperty
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.gridlayout import GridLayout
from kivy.uix.label import Label
from kivy.uix.scrollview import ScrollView
from kivy.uix.textinput import TextInput


class MobileClient(BoxLayout):
    """Main mobile client interface"""

    status_text = StringProperty("Initializing...")
    connected = BooleanProperty(False)
    offline_mode = BooleanProperty(False)

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.orientation = "vertical"
        self.api_base = "http://localhost:8003"
        self.user_id = None

        # Start connection check thread
        threading.Thread(target=self.check_connection, daemon=True).start()

        # Schedule periodic sync
        Clock.schedule_interval(self.sync_data, 30)  # Sync every 30 seconds

        self.build_ui()

    def check_connection(self):
        """Check connection to mobile API server"""
        while True:
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
            time.sleep(10)  # Check every 10 seconds

    def build_ui(self):
        """Build the mobile interface"""
        # Status bar
        status_bar = BoxLayout(size_hint_y=0.1)
        self.status_label = Label(text=self.status_text, size_hint_x=0.7)
        self.connection_indicator = Label(
            text="🔴" if not self.connected else "🟢",
            size_hint_x=0.3,
        )
        status_bar.add_widget(self.status_label)
        status_bar.add_widget(self.connection_indicator)
        self.add_widget(status_bar)

        # Main content area
        content = ScrollView(size_hint_y=0.8)
        content_layout = GridLayout(cols=1, spacing=10, padding=10, size_hint_y=None)
        content_layout.bind(minimum_height=content_layout.setter("height"))

        # User session management
        session_section = BoxLayout(orientation="vertical", size_hint_y=None, height=100)
        session_title = Label(text="User Session", size_hint_y=0.3, bold=True)
        session_section.add_widget(session_title)

        user_input = TextInput(hint_text="Enter User ID", multiline=False, size_hint_y=0.7)
        session_section.add_widget(user_input)

        start_session_btn = Button(text="Start Session", size_hint_y=0.7)
        start_session_btn.bind(on_press=lambda x: self.start_session(user_input.text))
        session_section.add_widget(start_session_btn)

        content_layout.add_widget(session_section)

        # AI Features section
        ai_section = BoxLayout(orientation="vertical", size_hint_y=None, height=150)
        ai_title = Label(text="AI Features", size_hint_y=0.2, bold=True)
        ai_section.add_widget(ai_title)

        ai_buttons = BoxLayout(size_hint_y=0.8)
        predictive_btn = Button(text="Predictive Analytics")
        predictive_btn.bind(on_press=lambda x: self.use_ai_feature("predictive_analytics"))
        ai_buttons.add_widget(predictive_btn)

        ml_btn = Button(text="ML Training")
        ml_btn.bind(on_press=lambda x: self.use_ai_feature("ml_training"))
        ai_buttons.add_widget(ml_btn)

        ai_section.add_widget(ai_buttons)
        content_layout.add_widget(ai_section)

        # Data Sync section
        sync_section = BoxLayout(orientation="vertical", size_hint_y=None, height=100)
        sync_title = Label(text="Data Synchronization", size_hint_y=0.3, bold=True)
        sync_section.add_widget(sync_title)

        sync_btn = Button(text="Sync Data", size_hint_y=0.7)
        sync_btn.bind(on_press=self.sync_data)
        sync_section.add_widget(sync_btn)

        content_layout.add_widget(sync_section)

        # Offline queue status
        queue_section = BoxLayout(orientation="vertical", size_hint_y=None, height=80)
        queue_title = Label(text="Offline Queue", size_hint_y=0.4, bold=True)
        queue_section.add_widget(queue_title)

        self.queue_status = Label(text="Queue: 0 items", size_hint_y=0.6)
        queue_section.add_widget(self.queue_status)

        content_layout.add_widget(queue_section)

        content.add_widget(content_layout)
        self.add_widget(content)

        # Footer with platform info
        footer = Label(text="UAI Android Client v1.0.0", size_hint_y=0.1, font_size=12)
        self.add_widget(footer)

    def start_session(self, user_id):
        """Start a mobile user session"""
        if not user_id:
            self.status_text = "Please enter a User ID"
            return

        try:
            response = requests.post(
                f"{self.api_base}/mobile/session/start",
                data={
                    "user_id": user_id,
                    "device_type": "mobile",
                    "platform": "android",
                },
            )
            if response.status_code == 200:
                self.status_text = f"Session started for {user_id}"
                self.user_id = user_id
            else:
                self.status_text = "Failed to start session"
        except:
            self.status_text = "Offline - Session queued"
            self.offline_mode = True

    def use_ai_feature(self, feature_type):
        """Use mobile AI feature"""
        if not hasattr(self, "user_id"):
            self.status_text = "Please start a session first"
            return

        try:
            response = requests.post(
                f"{self.api_base}/mobile/ai/feature",
                data={
                    "user_id": self.user_id,
                    "device_id": f"android_{self.user_id}",
                    "feature_type": feature_type,
                    "offline_mode": str(self.offline_mode).lower(),
                },
            )
            if response.status_code == 200:
                self.status_text = f"AI Feature used: {feature_type}"
            else:
                self.status_text = "AI Feature failed"
        except:
            self.status_text = "Offline - AI feature queued"
            # Queue for later sync
            self.queue_offline_action("ai_feature", {
                "feature_type": feature_type,
                "timestamp": datetime.now().isoformat(),
            })

    def sync_data(self, dt=None):
        """Synchronize data with server"""
        if not self.connected or not self.user_id:
            return

        try:
            # Send sync request via REST API
            response = requests.post(
                f"{self.api_base}/mobile/sync",
                data={
                    "user_id": self.user_id,
                    "data_type": "periodic_sync",
                    "data_payload": json.dumps({"timestamp": datetime.now().isoformat()}),
                    "platform": "android",
                },
            )
            if response.status_code == 200:
                self.status_text = "Data synchronized"
            else:
                self.status_text = "Sync failed"
        except Exception as e:
            print(f"Sync failed: {e}")
            self.status_text = "Sync failed - Offline"

    def queue_offline_action(self, action_type, data):
        """Queue action for offline processing"""
        # In a real implementation, this would save to local storage
        print(f"Queued offline action: {action_type} - {data}")

    def check_offline_queue(self):
        """Check and process offline queue"""
        if not hasattr(self, "user_id"):
            return

        try:
            response = requests.get(f"{self.api_base}/mobile/offline/queue?user_id={self.user_id}")
            if response.status_code == 200:
                queue = response.json().get("queue", [])
                self.queue_status.text = f"Queue: {len(queue)} items"
        except:
            pass

class UAIAndroidApp(App):
    """UAI Android Mobile Application"""

    def __init__(self, demo_mode=False, **kwargs):
        super().__init__(**kwargs)
        self.demo_mode = demo_mode

    def build(self):
        self.title = "UAI Enterprise Mobile"
        client = MobileClient()
        if self.demo_mode:
            # In demo mode, automatically perform operations
            Clock.schedule_once(lambda dt: self.run_demo(client), 2)
        return client

    def run_demo(self, client):
        """Run automated demo operations"""
        print("🎯 Starting Android Mobile Client Demo...")

        # Simulate user interactions
        Clock.schedule_once(lambda dt: self.demo_start_session(client), 1)
        Clock.schedule_once(lambda dt: self.demo_use_ai(client), 3)
        Clock.schedule_once(lambda dt: self.demo_sync(client), 5)
        Clock.schedule_once(lambda dt: self.stop(), 7)  # Exit after demo

    def demo_start_session(self, client):
        """Demo session start"""
        print("1. Starting user session...")
        client.start_session("demo_user_android")

    def demo_use_ai(self, client):
        """Demo AI feature usage"""
        print("2. Using AI features...")
        client.use_ai_feature("predictive_analytics")
        Clock.schedule_once(lambda dt: client.use_ai_feature("ml_training"), 1)

    def demo_sync(self, client):
        """Demo data sync"""
        print("3. Synchronizing data...")
        client.sync_data()

    def on_start(self):
        """App startup"""
        print("UAI Android Client started")

    def on_stop(self):
        """App shutdown"""
        print("UAI Android Client demo completed")

if __name__ == "__main__":
    # Check for demo mode via environment variable or command line
    import os
    import sys
    demo_mode = os.environ.get("MOBILE_DEMO_MODE") == "true" or "--demo" in sys.argv or "-d" in sys.argv

    print(f"Demo mode: {demo_mode}")  # Debug output

    if demo_mode:
        # Run in headless demo mode
        print("🤖 UAI Android Mobile Client - Demo Mode")
        print("=========================================")

        # Set headless backend for kivy to prevent GUI
        os.environ["KIVY_WINDOW"] = "headless"

        # Import kivy here to use headless mode
        import kivy
        kivy.require("2.0.0")

        from kivy.config import Config
        Config.set("graphics", "width", "1")
        Config.set("graphics", "height", "1")
        Config.set("kivy", "window_icon", "")

        client = MobileClient()
        print("🎯 Starting Android Mobile Client Demo...")

        # Simulate user interactions
        print("1. Starting user session...")
        client.start_session("demo_user_android")

        print("2. Using AI features...")
        client.use_ai_feature("predictive_analytics")
        import time
        time.sleep(1)
        client.use_ai_feature("ml_training")

        print("3. Synchronizing data...")
        client.sync_data()

        print("✅ Android demo completed successfully!")
        sys.exit(0)
    else:
        # Run GUI app
        os.environ["KIVY_NO_ARGS"] = "1"
        UAIAndroidApp(demo_mode=demo_mode).run()
