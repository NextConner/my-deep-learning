#!/usr/bin/env python3
import json

def create_element(id, type, x, y, **kwargs):
    elem = {
        "id": id, "type": type, "x": x, "y": y, "angle": 0,
        "strokeColor": kwargs.get("strokeColor", "#1e1e1e"),
        "backgroundColor": kwargs.get("backgroundColor", "transparent"),
        "fillStyle": "hachure", "strokeWidth": 2, "strokeStyle": "solid",
        "roughness": kwargs.get("roughness", 1), "opacity": 100,
        "groupIds": [], "frameId": None, "roundness": None,
        "seed": 1, "version": 1, "versionNonce": 1,
        "isDeleted": False, "updated": 1, "link": None,
        "locked": False, "boundElements": None
    }

    if type == "text":
        elem.update({
            "fontFamily": kwargs.get("fontFamily", 1),
            "textAlign": "left", "verticalAlign": "top",
            "containerId": None, "originalText": kwargs.get("text", ""),
            "baseline": kwargs.get("fontSize", 14)
        })

    if type == "arrow":
        width = kwargs.get("width", 0)
        height = kwargs.get("height", 0)
        elem.update({
            "points": [[0, 0], [width, height]],
            "lastCommittedPoint": None, "startBinding": None,
            "endBinding": None, "startArrowhead": None,
            "endArrowhead": "arrow"
        })

    elem.update(kwargs)
    return elem

elements = []

# Title
elements.append(create_element("title", "text", 400, 50,
    text="JTCool ERP + AI Assistant Architecture", fontSize=24, fontFamily=3, width=600, height=35))

# Frontend Layer
elements.append(create_element("fe1_box", "rectangle", 100, 150,
    width=300, height=80, backgroundColor="#a5d8ff", strokeColor="#1971c2"))
elements.append(create_element("fe1_text", "text", 180, 175,
    text="Frontend Layer\nVue 3 + Element Plus\n(jtcool-front)", fontSize=16, width=200, height=60))

elements.append(create_element("fe2_box", "rectangle", 500, 150,
    width=300, height=80, backgroundColor="#a5d8ff", strokeColor="#1971c2"))
elements.append(create_element("fe2_text", "text", 560, 175,
    text="AI Assistant UI\nVue 3 + SSE\n(assist-app)", fontSize=16, width=200, height=60))

# API Gateway
elements.append(create_element("gw_box", "rectangle", 50, 300,
    width=800, height=60, backgroundColor="#ffd43b", strokeColor="#f08c00"))
elements.append(create_element("gw_text", "text", 300, 320,
    text="API Gateway / Security Layer (Spring Security + JWT)", fontSize=16, width=500, height=25))

# Backend Modules
elements.append(create_element("oms_box", "rectangle", 50, 420,
    width=180, height=100, backgroundColor="#b2f2bb", strokeColor="#2f9e44"))
elements.append(create_element("oms_text", "text", 80, 455,
    text="OMS Module\n订单管理系统", fontSize=14, width=120, height=40))

elements.append(create_element("wms_box", "rectangle", 250, 420,
    width=180, height=100, backgroundColor="#b2f2bb", strokeColor="#2f9e44"))
elements.append(create_element("wms_text", "text", 280, 455,
    text="WMS Module\n仓库管理系统", fontSize=14, width=120, height=40))

elements.append(create_element("prod_box", "rectangle", 450, 420,
    width=180, height=100, backgroundColor="#b2f2bb", strokeColor="#2f9e44"))
elements.append(create_element("prod_text", "text", 470, 455,
    text="Product Module\n产品档案管理", fontSize=14, width=140, height=40))

elements.append(create_element("ai_box", "rectangle", 650, 420,
    width=200, height=100, backgroundColor="#ffc9c9", strokeColor="#c92a2a"))
elements.append(create_element("ai_text", "text", 670, 445,
    text="AI Assister\nLangChain4j\nRAG + Agents", fontSize=14, width=160, height=60))

# Database Layer
elements.append(create_element("db1_box", "rectangle", 50, 580,
    width=380, height=120, backgroundColor="#e7f5ff", strokeColor="#1864ab"))
elements.append(create_element("db1_text", "text", 100, 620,
    text="PostgreSQL Database\n- ERP业务数据\n- 用户权限数据", fontSize=14, width=300, height=60))

elements.append(create_element("db2_box", "rectangle", 470, 580,
    width=380, height=120, backgroundColor="#ffe3e3", strokeColor="#c92a2a"))
elements.append(create_element("db2_text", "text", 510, 610,
    text="PostgreSQL + pgvector\n- 向量数据库\n- RAG知识库\n- 审计日志", fontSize=14, width=300, height=80))

# Security Panel
elements.append(create_element("sec_box", "rectangle", 900, 150,
    width=200, height=350, backgroundColor="#fff5f5", strokeColor="#e03131"))
elements.append(create_element("sec_title", "text", 920, 170,
    text="Security Features", fontSize=16, fontFamily=3, width=160, height=25))
elements.append(create_element("sec_text", "text", 920, 210,
    text="双鉴权模式:\n- local-jwt\n- enterprise-jwt\n\n安全网关:\n- 输入检测\n- 工具权限网关\n- 输出脱敏\n\n审计日志:\n- audit_log\n- 操作追踪\n\nToken配额:\n- 使用量监控\n- 限流控制",
    fontSize=12, width=160, height=280))

# External Services
elements.append(create_element("ext_box", "rectangle", 900, 520,
    width=200, height=100, backgroundColor="#fff3bf", strokeColor="#f59f00"))
elements.append(create_element("ext_text", "text", 920, 545,
    text="External Services\n- LLM API\n- Embedding API\n- Monitoring", fontSize=14, width=160, height=70))

# Observability
elements.append(create_element("obs_box", "rectangle", 900, 640,
    width=200, height=170, backgroundColor="#d0ebff", strokeColor="#1971c2"))
elements.append(create_element("obs_title", "text", 920, 660,
    text="Observability", fontSize=16, fontFamily=3, width=160, height=25))
elements.append(create_element("obs_text", "text", 920, 700,
    text="- Micrometer\n- Prometheus\n- Zipkin\n- Resilience4j\n- 链路追踪\n- 性能监控",
    fontSize=12, width=160, height=100))

# Framework Layer
elements.append(create_element("fw_box", "rectangle", 50, 750,
    width=800, height=170, backgroundColor="#f3f0ff", strokeColor="#7950f2"))
elements.append(create_element("fw_title", "text", 80, 770,
    text="Core Framework & Common Modules", fontSize=16, fontFamily=3, width=400, height=25))
elements.append(create_element("fw_text", "text", 80, 810,
    text="- jtcool-common: 公共工具类、常量、异常处理\n- jtcool-framework: 框架配置、拦截器、过滤器\n- jtcool-system: 系统管理、用户权限、菜单管理\n- jtcool-admin: 后台管理入口、统一配置",
    fontSize=12, width=700, height=100))

# Arrows
elements.append(create_element("arrow1", "arrow", 250, 230, width=0, height=70, strokeColor="#1971c2"))
elements.append(create_element("arrow2", "arrow", 650, 230, width=0, height=70, strokeColor="#1971c2"))
elements.append(create_element("arrow3", "arrow", 140, 360, width=0, height=60, strokeColor="#2f9e44"))
elements.append(create_element("arrow4", "arrow", 340, 360, width=0, height=60, strokeColor="#2f9e44"))
elements.append(create_element("arrow5", "arrow", 540, 360, width=0, height=60, strokeColor="#2f9e44"))
elements.append(create_element("arrow6", "arrow", 750, 360, width=0, height=60, strokeColor="#c92a2a"))
elements.append(create_element("arrow7", "arrow", 140, 520, width=100, height=60, strokeColor="#1864ab"))
elements.append(create_element("arrow8", "arrow", 340, 520, width=-100, height=60, strokeColor="#1864ab"))
elements.append(create_element("arrow9", "arrow", 750, 520, width=-90, height=60, strokeColor="#c92a2a"))
elements.append(create_element("arrow10", "arrow", 850, 470, width=50, height=0, strokeColor="#f59f00"))

# Export
excalidraw_data = {
    "type": "excalidraw",
    "version": 2,
    "source": "https://excalidraw.com",
    "elements": elements,
    "appState": {"gridSize": None, "viewBackgroundColor": "#ffffff"},
    "files": {}
}

with open("jtcool-architecture.excalidraw", 'w', encoding='utf-8') as f:
    json.dump(excalidraw_data, f, ensure_ascii=False, indent=2)

print("Architecture diagram created: jtcool-architecture.excalidraw")

