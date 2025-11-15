// ========================================
// SCROLL ANIMATIONS
// ========================================

// Fade in elements on scroll
function initScrollAnimations() {
  const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -100px 0px'
  };

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible');
      }
    });
  }, observerOptions);

  // Observe all elements with fade-in-scroll class
  document.querySelectorAll('.fade-in-scroll').forEach(el => {
    observer.observe(el);
  });
}

// ========================================
// LOADING ANIMATION
// ========================================

function showLoading() {
  const loader = document.createElement('div');
  loader.id = 'page-loader';
  loader.innerHTML = `
    <div style="
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(255, 255, 255, 0.95);
      display: flex;
      justify-content: center;
      align-items: center;
      z-index: 9999;
    ">
      <div style="
        width: 60px;
        height: 60px;
        border: 4px solid #f3f3f3;
        border-top: 4px solid #667eea;
        border-radius: 50%;
        animation: spin 1s linear infinite;
      "></div>
    </div>
    <style>
      @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
      }
    </style>
  `;
  
  document.body.appendChild(loader);
}

function hideLoading() {
  const loader = document.getElementById('page-loader');
  if (loader) {
    loader.style.opacity = '0';
    setTimeout(() => loader.remove(), 300);
  }
}

// ========================================
// PRODUCT CARD ANIMATIONS
// ========================================

function initProductAnimations() {
  // Add hover effect to product images
  document.querySelectorAll('.product-item').forEach((item, index) => {
    // Stagger animation
    item.style.animationDelay = `${index * 0.1}s`;
    
    // Add wishlist icon if not exists
    const imgContainer = item.querySelector('.product-img');
    if (imgContainer && !imgContainer.querySelector('.wishlist-icon')) {
      const wishlistBtn = document.createElement('button');
      wishlistBtn.className = 'wishlist-icon';
      wishlistBtn.innerHTML = '<i class="fa fa-heart-o"></i>';
      wishlistBtn.style.cssText = `
        position: absolute;
        top: 10px;
        right: 10px;
        background: white;
        border: none;
        border-radius: 50%;
        width: 40px;
        height: 40px;
        cursor: pointer;
        opacity: 0;
        transition: all 0.3s ease;
        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        z-index: 10;
      `;
      
      imgContainer.style.position = 'relative';
      imgContainer.appendChild(wishlistBtn);
      
      // Show on hover
      item.addEventListener('mouseenter', () => {
        wishlistBtn.style.opacity = '1';
      });
      
      item.addEventListener('mouseleave', () => {
        wishlistBtn.style.opacity = '0';
      });
      
      // Toggle heart on click
      wishlistBtn.addEventListener('click', (e) => {
        e.preventDefault();
        const icon = wishlistBtn.querySelector('i');
        if (icon.classList.contains('fa-heart-o')) {
          icon.classList.remove('fa-heart-o');
          icon.classList.add('fa-heart');
          wishlistBtn.style.color = '#f5576c';
        } else {
          icon.classList.remove('fa-heart');
          icon.classList.add('fa-heart-o');
          wishlistBtn.style.color = '#333';
        }
      });
    }
  });
}

// ========================================
// NOTIFICATION TOAST
// ========================================

function showToast(message, type = 'success') {
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    background: ${type === 'success' ? 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)' : 'linear-gradient(135deg, #ee0979 0%, #ff6a00 100%)'};
    color: white;
    padding: 15px 20px;
    border-radius: 8px;
    box-shadow: 0 4px 15px rgba(0,0,0,0.2);
    z-index: 9999;
    animation: slideInRight 0.5s ease;
    max-width: 300px;
  `;
  toast.textContent = message;
  
  document.body.appendChild(toast);
  
  setTimeout(() => {
    toast.style.animation = 'slideOutRight 0.5s ease';
    setTimeout(() => toast.remove(), 500);
  }, 3000);
}

// ========================================
// IMAGE LAZY LOADING
// ========================================

function initLazyLoading() {
  const images = document.querySelectorAll('img[data-src]');
  
  const imageObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const img = entry.target;
        img.src = img.dataset.src;
        img.removeAttribute('data-src');
        imageObserver.unobserve(img);
      }
    });
  });
  
  images.forEach(img => imageObserver.observe(img));
}

// ========================================
// INITIALIZE ALL ON PAGE LOAD
// ========================================

document.addEventListener('DOMContentLoaded', () => {
  // Show loading
  showLoading();
  
  // Initialize features
  setTimeout(() => {
    initScrollAnimations();
    initProductAnimations();
    initLazyLoading();
    hideLoading();
  }, 500);
});

// ========================================
// ADD ANIMATIONS TO EXISTING ELEMENTS
// ========================================

window.addEventListener('load', () => {
  // Add fade-in-scroll class to products
  document.querySelectorAll('.product-item').forEach(item => {
    if (!item.classList.contains('fade-in-scroll')) {
      item.classList.add('fade-in-scroll');
    }
  });
  
  // Add hover-lift to cards
  document.querySelectorAll('.single-banner, .single-category').forEach(item => {
    if (!item.classList.contains('hover-lift')) {
      item.classList.add('hover-lift');
    }
  });
});
