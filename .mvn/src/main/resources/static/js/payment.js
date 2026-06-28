function showLoadingModal() {
    var overlay = document.createElement('div');
    overlay.id = 'loading-overlay';
    overlay.innerHTML = `
        <div style="background:white;padding:30px 40px;border-radius:12px;text-align:center;">
            <div style="display:inline-block;width:40px;height:40px;border:4px solid #f0f0f0;
                        border-top-color:#1890ff;border-radius:50%;animation:spin 0.8s linear infinite;"></div>
            <p style="margin-top:16px;color:#666;">支付处理中...</p>
        </div>
    `;
    overlay.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.4);display:flex;align-items:center;justify-content:center;z-index:9998;';
    document.body.appendChild(overlay);

    if (!document.getElementById('loading-style')) {
        var style = document.createElement('style');
        style.id = 'loading-style';
        style.textContent = '@keyframes spin { to { transform: rotate(360deg); } }';
        document.head.appendChild(style);
    }
}

function removeLoadingModal() {
    var el = document.getElementById('loading-overlay');
    if (el) el.remove();
}

function showSuccessModal(orderIds, amount) {
    var oldModal = document.querySelector('.payment-success-modal');
    if (oldModal) oldModal.remove();

    var orderDisplay = Array.isArray(orderIds) ? orderIds.length + '个订单' : '订单 ' + orderIds;

    var overlay = document.createElement('div');
    overlay.className = 'payment-success-overlay';

    var modal = document.createElement('div');
    modal.className = 'payment-success-modal';
    modal.innerHTML = `
        <div class="success-icon">
            <svg viewBox="0 0 100 100" width="80" height="80">
                <circle cx="50" cy="50" r="45" fill="none" stroke="#52c41a" stroke-width="5"/>
                <path d="M30 50 L45 65 L70 35" stroke="#52c41a" stroke-width="6" fill="none" 
                      stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
        </div>
        <h2 class="success-title">🎉 支付成功！</h2>
        <p class="success-detail">${orderDisplay} 已支付</p>
        <p class="success-detail">支付金额：<strong style="color:#ff4d4f;">¥${amount.toFixed(2)}</strong></p>
        <p class="success-tip">即将跳转到主页...</p>
        <button class="success-btn" onclick="window.location.href='/'">
            回到主页
        </button>
    `;

    overlay.appendChild(modal);
    document.body.appendChild(overlay);

    var style = document.createElement('style');
    style.textContent = `
        .payment-success-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.5);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 9999;
            animation: fadeIn 0.3s ease;
        }
        .payment-success-modal {
            background: white;
            border-radius: 16px;
            padding: 40px 50px;
            text-align: center;
            min-width: 380px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            animation: slideUp 0.4s ease;
        }
        .success-icon svg {
            animation: scaleIn 0.5s ease;
        }
        .success-title {
            color: #333;
            margin: 16px 0 8px;
            font-size: 24px;
        }
        .success-detail {
            color: #666;
            margin: 8px 0;
            font-size: 15px;
        }
        .success-tip {
            color: #999;
            font-size: 13px;
            margin: 16px 0 20px;
        }
        .success-btn {
            background: #52c41a;
            color: white;
            border: none;
            padding: 10px 40px;
            border-radius: 8px;
            font-size: 16px;
            cursor: pointer;
            transition: background 0.2s;
        }
        .success-btn:hover {
            background: #389e0d;
        }
        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }
        @keyframes slideUp {
            from { transform: translateY(30px); opacity: 0; }
            to { transform: translateY(0); opacity: 1; }
        }
        @keyframes scaleIn {
            0% { transform: scale(0); }
            80% { transform: scale(1.1); }
            100% { transform: scale(1); }
        }
    `;
    document.head.appendChild(style);
}

function handlePaymentSuccess(orderIds, totalAmount) {
    showSuccessModal(orderIds, totalAmount);

    setTimeout(function() {
        window.location.href = '/';
    }, 3000);
}

function handlePayment(orderIds, amount) {
    showLoadingModal();

    setTimeout(function() {
        removeLoadingModal();
        handlePaymentSuccess(orderIds, amount);
    }, 4500);
}