(function() {
  'use strict';
  var app;

  app = angular.module('app', ['ui.router', 'ui.bootstrap', 'ngCookies', 'ngAnimate', 'app.services', 'app.controllers', 'app.directives', 'app.utils', 'app.filters']);

  app.run([
    '$rootScope', '$rootElement', '$state', '$stateParams', 'FlashService', function($rootScope, $rootElement, $state, $stateParams, FlashService) {
      $rootScope.$state = $state;
      $rootScope.$stateParams = $stateParams;
      $rootElement.off('click');
      $rootScope.$on('$stateChangeStart', function() {
        return FlashService.clear();
      });
      return $rootScope.$on('$startChangeError', function() {
        return FlashService.show("There was an routing error");
      });
    }
  ]);

  app.config([
    '$stateProvider', '$urlRouterProvider', 'RestangularProvider', function($stateProvider, $urlRouterProvider, RestangularProvider) {
      RestangularProvider.setResponseExtractor(function(response, operation, what, url) {
        var newResponse;
        if (operation === 'getList' && 'cartItems' in response) {
          newResponse = response.cartItems;
          newResponse.id = response.id;
          return newResponse;
        }
        return response;
      });
      return $stateProvider.state('product', {
        url: '/browse',
        templateUrl: '/assets/angular/product-list.html',
        controller: 'BrowseCtrl',
        resolve: {
          page: function(Api) {
            return Api.one('product').get();
          }
        }
      }).state('home', {
        url: '/home',
        templateUrl: '/assets/angular/home.html'
      }).state('dude', {
        url: '/dude',
        templateUrl: '/assets/angular/dude-list.html',
        controller: 'DudeCtrl',
        resolve: {
          dudes: function(Api) {
            return Api.all('dude').getList();
          }
        }
      }).state('dude.detail', {
        url: '/{id:[0-9]+}',
        templateUrl: 'assets/angular/dude-detail.html',
        controller: 'DudeDetailCtrl',
        resolve: {
          dude: function($stateParams, Utils, dudes) {
            return Utils.fromId(dudes, $stateParams.id);
          },
          dudes: function(dudes) {
            return dudes;
          }
        }
      });
    }
  ]);

}).call(this);

(function() {
  'use strict';
  var app;

  app = angular.module('app.controllers', []);

  app.controller('DudeCtrl', [
    '$scope', 'Api', 'dudes', function($scope, Api, dudes) {
      $scope.dudes = dudes;
      $scope.newDude = {};
      $scope.reversed = false;
      $scope.toggleReversed = function() {
        return $scope.reversed = !$scope.reversed;
      };
      $scope.processForm = function() {
        return Api.all('dude').post($scope.newDude).then(function(dude) {
          $scope.dudes.push(dude);
          return $scope.newDude = {};
        }, function() {
          return console.error("error error");
        });
      };
      return $scope.selectDude = function(selectedDude) {
        return angular.forEach($scope.dudes, function(dude) {
          return dude.selected = dude === selectedDude;
        });
      };
    }
  ]);

  app.controller('DudeDetailCtrl', [
    '$scope', 'Api', 'dudes', 'dude', 'Utils', function($scope, Api, dudes, selectedDude, Utils) {
      var closeEdit;
      $scope.dude = selectedDude;
      closeEdit = function() {
        $scope.editDude = {};
        return $scope.isEdit = false;
      };
      closeEdit();
      $scope.closeEdit = closeEdit;
      $scope.deleteDude = function() {
        return selectedDude.remove().then(function() {
          var idx;
          idx = Utils.indexById(dudes, selectedDude);
          dudes.splice(idx, 1);
          return $scope.$state.go('dude');
        });
      };
      $scope.openForEdit = function() {
        $scope.isEdit = true;
        return $scope.editDude = Api.copy($scope.dude);
      };
      return $scope.updateDude = function() {
        var editDude;
        editDude = $scope.editDude;
        return editDude.put().then(function(dude) {
          var idx;
          idx = Utils.indexById(dudes, dude);
          dudes.splice(idx, 1, dude);
          $scope.dude = dude;
          return closeEdit();
        }, function() {
          return console.error("error error putting");
        });
      };
    }
  ]);

  app.controller('GenericFormCtrl', ['$scope', function($scope) {}]);

  app.controller('BrowseCtrl', [
    '$scope', 'FlashService', 'Api', 'page', 'Utils', 'CartService', '$modal', function($scope, FlashService, Api, page, Utils, CartService, $modal) {
      var setValues;
      $scope.page = page;
      $scope.pageRange = null;
      $scope.filters = {
        page: 1,
        size: 10,
        sort: 'name',
        order: 'asc',
        filter: '',
        category: ''
      };
      $scope.update = function() {
        $scope.buttonText = 'Loading...';
        return Api.one('product').get($scope.filters).then(function(page) {
          return setValues(page);
        }, function() {
          return FlashService.show("Error loading products");
        });
      };
      setValues = function(page) {
        $scope.page = page;
        $scope.range = Utils.chunkIt(page.products, 4);
        FlashService.show("Showing " + page.products.length + " results of " + page.totalRowCount, "info");
        return $scope.buttonText = 'Update';
      };
      setValues(page);
      $scope.goTo = function(pageNum) {
        if (pageNum !== $scope.page.pageIndex) {
          $scope.filters.page = pageNum;
          return $scope.update();
        }
      };
      $scope.buy = CartService.add;
      $scope.buyStatus = CartService.buyStatus;
      $scope.showCart = function() {
        return $scope.$emit('showCart');
      };
      return $scope.showDetail = function(product) {
        var modalInstance;
        modalInstance = $modal.open({
          templateUrl: '/assets/angular/product-modal.html',
          controller: 'ProductDetailCtrl',
          resolve: {
            product: function() {
              return product;
            }
          }
        });
        return modalInstance.result.then(function() {
          return $scope.showCart();
        });
      };
    }
  ]);

  app.controller('ProductDetailCtrl', [
    '$scope', '$modalInstance', 'product', 'CartService', function($scope, $self, product, CartService) {
      $scope.product = product;
      $scope.buyStatus = CartService.buyStatus;
      $scope.buy = CartService.add;
      $scope.showCart = function() {
        return $self.close();
      };
      return $scope.close = function() {
        return $self.dismiss('cancel');
      };
    }
  ]);

  app.controller('CartCtrl', [
    '$scope', 'CartService', 'Api', '$modal', '$rootScope', '$timeout', function($scope, CartService, Api, $modal, $rootScope, $timeout) {
      $scope.cart = CartService.cart;
      $scope.doFlash = false;
      $scope.showCart = function() {
        return $modal.open({
          templateUrl: '/assets/angular/cart-modal.html',
          controller: 'CartModalCtrl',
          resolve: {
            cart: function() {
              return CartService.cart;
            }
          }
        });
      };
      $scope.$on('itemAdded', function(event, obj) {
        $scope.doFlash = true;
        return $timeout(function() {
          return $scope.doFlash = false;
        }, 2000);
      });
      return $rootScope.$on('showCart', function(event, obj) {
        return $scope.showCart();
      });
    }
  ]);

  app.controller('CartModalCtrl', [
    '$scope', '$modalInstance', 'Api', 'cart', 'Utils', function($scope, $modalInstance, Api, cart, Utils) {
      $scope.cart = cart;
      $scope.editable = {};
      $scope.close = function() {
        return $modalInstance.dismiss('cancel');
      };
      $scope.update = function() {
        $scope.errors = null;
        return $scope.editable.put().then(function(item) {
          var idx;
          idx = Utils.indexById($scope.cart, item.id);
          $scope.cart.splice(idx, 1, item);
          return $scope.editable = {};
        }, function(result) {
          $scope.errors = result.data;
          console.error("error updating " + item);
          return $scope.editable = {};
        });
      };
      $scope.openForEdit = function(index) {
        return $scope.editable = Api.copy($scope.cart[index]);
      };
      return $scope.remove = function(index) {
        var item;
        item = $scope.cart[index];
        return item.remove().then(function() {
          return $scope.cart.splice(index, 1);
        }, function() {
          return console.error("error removing " + item + " from cart");
        });
      };
    }
  ]);

  app.controller('CheckoutCtrl', [
    '$scope', 'CartService', function($scope, CartService) {
      return $scope.cart = CartService.cart;
    }
  ]);

}).call(this);

(function() {
  var app;

  app = angular.module('app.directives', []);

  app.directive('pwCheck', function() {
    return {
      require: 'ngModel',
      link: function(scope, elem, attrs, ctrl) {
        var firstPwd;
        firstPwd = '#' + attrs.pwCheck;
        return elem.add(firstPwd).on('keyup', function() {
          return scope.$apply(function() {
            var v;
            v = elem.val() === $(firstPwd).val();
            return ctrl.$setValidity('pwdmatch', v);
          });
        });
      }
    };
  });

  app.directive('onEnter', function() {
    return function(scope, element, attrs) {
      return element.bind("keydown keypress", function(event) {
        if (event.which === 13) {
          return scope.$apply(function() {
            scope.$eval(attrs.onEnter);
            return event.preventDefault();
          });
        }
      });
    };
  });

  app.directive('lowestNumber', function() {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function(scope, element, attrs, ctrl) {
        var lowestNumber;
        if (attrs.lowestNumber) {
          lowestNumber = parseInt(attrs.lowestNumber, 10);
        } else {
          lowestNumber = 1;
        }
        return scope.$watch(attrs.ngModel, function(newVal, oldVal) {
          var num;
          if (newVal !== void 0) {
            num = parseInt(newVal, 10);
            if (isNaN(num) || num < lowestNumber) {
              ctrl.$setViewValue(lowestNumber);
              return ctrl.$render();
            }
          }
        });
      }
    };
  });

  app.directive('initial', function() {
    return {
      restrict: 'A',
      controller: [
        '$scope', '$element', '$attrs', '$parse', function($scope, $element, $attrs, $parse) {
          var getter, setter, val;
          val = $attrs.initial || $attrs.value || $element.text();
          getter = $parse($attrs.ngModel);
          setter = getter.assign;
          return setter($scope, val);
        }
      ]
    };
  });

}).call(this);

(function() {
  var app;

  app = angular.module('app.filters', []);

  app.filter('capitalize', function() {
    return function(input, scope) {
      var val;
      val = input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
      return val.replace('_', ' ');
    };
  });

}).call(this);

(function() {
  'use strict';
  var api;

  api = angular.module('app.services', ['restangular']);

  api.factory('Api', [
    'Restangular', function(Restangular) {
      return Restangular.withConfig(function(RestangularConfigurer) {
        return RestangularConfigurer.setBaseUrl('/api');
      });
    }
  ]);

  api.factory('UserService', [
    'USER', function(USER) {
      return {
        isLoggedIn: function() {
          return USER && USER.email;
        },
        isAdmin: function() {
          return USER && USER.email && USER.isAdmin;
        },
        email: USER.email
      };
    }
  ]);

  api.factory('FlashService', [
    '$rootScope', function($rootScope) {
      return {
        show: function(message, type) {
          if (type == null) {
            type = "danger";
          }
          $rootScope.flash = message;
          return $rootScope.flashClass = type;
        },
        clear: function() {
          $rootScope.flash = '';
          return $rootScope.flashClass = '';
        }
      };
    }
  ]);

  api.factory('CartService', [
    'Api', 'UserService', '$rootScope', 'FlashService', function(Api, UserService, $rootScope, FlashService) {
      var buy, buyStatus, cart, ownedByUser, setCart;
      cart = [];
      setCart = function() {
        if (UserService.isLoggedIn()) {
          return Api.all('cart').getList().then(function(incCart) {
            return angular.copy(incCart, cart);
          });
        }
      };
      setCart();
      buy = function(product) {
        return Api.all('cart').post({
          id: product.id
        }).then(function(cartItem) {
          cart.push(cartItem);
          return $rootScope.$broadcast('itemAdded');
        }, function(error) {
          return FlashService.show(error);
        });
      };
      buyStatus = function(product) {
        if (!UserService.isLoggedIn()) {
          return "login";
        } else if (ownedByUser(product)) {
          return "owned";
        } else {
          return "buy";
        }
      };
      ownedByUser = function(product) {
        var owned;
        owned = false;
        angular.forEach(cart, function(item) {
          if (item.product.id === product.id) {
            return owned = true;
          }
        });
        return owned;
      };
      return {
        cart: cart,
        reset: function() {
          return setCart();
        },
        add: function(product) {
          return buy(product);
        },
        remove: function(item) {
          return remove(item);
        },
        buyStatus: function(product) {
          return buyStatus(product);
        }
      };
    }
  ]);

  api.factory('SubService', function() {
    var cache;
    cache = {};
    return {
      publish: function(topic, args) {
        return cache[topic] && angular.forEach(cache[topic], function(func) {
          return func.apply(null, args || []);
        });
      },
      subscribe: function(topic, callback) {
        if (!cache[topic]) {
          cache[topic] = [];
        }
        cache[topic].push(callback);
        return [topic, callback];
      },
      unsubscribe: function(handle) {
        var t;
        t = handle[0];
        return cache[t] && angular.forEach(cache[t](function(val, idx) {
          if (val === handle[1]) {
            return cache[t].splice(idx, 1);
          }
        }));
      }
    };
  });

}).call(this);

(function() {
  var app;

  app = angular.module('app.utils', []);

  app.factory('Utils', function() {
    return {
      indexById: function(iterable, checkId) {
        var index;
        index = -1;
        angular.forEach(iterable, function(obj, i) {
          console.log;
          if (obj.id === checkId) {
            return index = i;
          }
        });
        return index;
      },
      fromId: function(iterable, checkId) {
        var the_obj;
        the_obj = {};
        angular.forEach(iterable, function(obj) {
          if (obj.id === checkId) {
            return the_obj = obj;
          }
        });
        return the_obj;
      },
      chunkIt: function(iterable, cols) {
        var i, _i, _ref, _results;
        _results = [];
        for (i = _i = 0, _ref = iterable.length; cols > 0 ? _i <= _ref : _i >= _ref; i = _i += cols) {
          _results.push(i);
        }
        return _results;
      }
    };
  });

}).call(this);
