import subprocess
import time
import os
import sys

# Configuration
JAVA_HOME = r'C:\Program Files\Java\jdk-23'
os.environ['JAVA_HOME'] = JAVA_HOME
MVNW = "mvnw.cmd" if sys.platform == "win32" else "./mvnw"

SERVICES = [
    {"name": "Discovery Server", "dir": "discovery-server", "port": 8761},
    {"name": "API Gateway", "dir": "api-gateway", "port": 8080},
    {"name": "Auth Service", "dir": "auth-service", "port": 8081},
    {"name": "Contact Service", "dir": "contact-service", "port": 8083},
    {"name": "Campaign Service", "dir": "campaign-service", "port": 8084},
    {"name": "Messaging Service", "dir": "messaging-service", "port": 8085},
    {"name": "Delivery Report Service", "dir": "delivery-report-service", "port": 8086},
    {"name": "Billing Service", "dir": "billing-service", "port": 8087},
    {"name": "Notification Service", "dir": "notification-service", "port": 8088},
    {"name": "Scheduler Service", "dir": "scheduler-service", "port": 8089},
]

def run_command(command, cwd=None, background=True):
    if background:
        return subprocess.Popen(command, cwd=cwd, shell=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    else:
        return subprocess.run(command, cwd=cwd, shell=True)

def main():
    print(" Starting Bulk SMS Service System...")

    # 1. Start Docker Infrastructure
    print(" Starting Docker infrastructure (Postgres, RabbitMQ)...")
    run_command("docker-compose up -d", background=False)
    
    processes = []

    # 2. Start Discovery Server first
    print(" Starting Discovery Server (Eureka)...")
    discovery = run_command(f"..\\{MVNW} spring-boot:run", cwd="discovery-server")
    processes.append(discovery)
    
    print(" Waiting for Discovery Server to initialize (15s)...")
    time.sleep(15)

    # 3. Start Business Services
    for service in SERVICES[1:]:
        print(f" Starting {service['name']}...")
        proc = run_command(f"..\\{MVNW} spring-boot:run", cwd=service['dir'])
        processes.append(proc)
        time.sleep(2) # Small delay to avoid CPU spike

    # 4. Start Frontend
    print("Starting Frontend Dashboard...")
    frontend = run_command("python -m http.server 8000", cwd="frontend-dashboard")
    processes.append(frontend)

    print("\nAll services are starting in the background!")
    print("🔗 Frontend: http://localhost:8000")
    print("🔗 Eureka:   http://localhost:8761")
    print("\nKeep this terminal open to keep the services running. Press Ctrl+C to stop all.")

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("\nStopping all services...")
        for proc in processes:
            proc.terminate()
        print(" Goodbye!")

if __name__ == "__main__":
    main()
