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
  var app,
    __indexOf = [].indexOf || function(item) { for (var i = 0, l = this.length; i < l; i++) { if (i in this && this[i] === item) return i; } return -1; };

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
          return console.log("error error");
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
          return console.log("error error putting");
        });
      };
    }
  ]);

  app.controller('SignupFormCtrl', [
    '$scope', function($scope) {
      return $scope.message = "Hello Form!";
    }
  ]);

  app.controller('BrowseCtrl', [
    '$scope', 'UserService', 'FlashService', 'Api', 'page', 'Utils', 'CartService', function($scope, UserService, FlashService, Api, page, Utils, CartService) {
      var ownedByUser, setValues;
      $scope.page = page;
      $scope.pageRange = null;
      $scope.filters = {
        page: 1,
        size: 10,
        sort: 'name',
        order: 'asc',
        filter: '',
        pType: ''
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
        var _i, _ref, _results;
        $scope.page = page;
        $scope.range = Utils.chunkIt(page.products, 4);
        FlashService.show("Showing " + page.products.length + " results of " + page.totalRowCount, "info");
        $scope.pageRange = page.totalPages > 0 ? (function() {
          _results = [];
          for (var _i = 1, _ref = page.totalPages; 1 <= _ref ? _i <= _ref : _i >= _ref; 1 <= _ref ? _i++ : _i--){ _results.push(_i); }
          return _results;
        }).apply(this) : [1];
        return $scope.buttonText = 'Update';
      };
      setValues(page);
      $scope.goTo = function(pageNum) {
        if (pageNum !== $scope.page.pageIndex && __indexOf.call($scope.pageRange, pageNum) >= 0) {
          $scope.filters.page = pageNum;
          return $scope.update();
        }
      };
      $scope.buy = function(product) {
        return CartService.add(product);
      };
      $scope.buyStatus = function(product) {
        if (!UserService.isLoggedIn()) {
          return "login";
        } else if (ownedByUser(product)) {
          return "owned";
        } else {
          return "buy";
        }
      };
      return ownedByUser = function(product) {
        var owned;
        owned = false;
        angular.forEach(CartService.cart.cartItems, function(item) {
          if (item.product.id === product.id) {
            return owned = true;
          }
        });
        return owned;
      };
    }
  ]);

  app.controller('CartCtrl', [
    '$scope', 'CartService', 'Api', '$modal', function($scope, CartService, Api, $modal) {
      $scope.cart = CartService.cart;
      console.log($scope.cart);
      return $scope.showCart = function() {
        var modalInstance;
        return modalInstance = $modal.open({
          templateUrl: '/assets/angular/cart-modal.html',
          controller: 'CartModalCtrl',
          resolve: {
            cart: function() {
              return CartService.cart;
            }
          }
        });
      };
    }
  ]);

  app.controller('CartModalCtrl', [
    '$scope', '$modalInstance', 'Api', 'cart', function($scope, $modalInstance, Api, cart) {
      console.log(cart);
      $scope.cart = Api.copy(cart);
      $scope.close = function() {
        return $modalInstance.dismiss('cancel');
      };
      $scope.update = function(index) {
        var item;
        item = $scope.cart.cartItems[index];
        console.log($scope.cart);
        return $scope.cart.customPUT(item, '');
      };
      return $scope.remove = function(index) {
        var product;
        product = $scope.cart.cartItems[index];
        console.log($scope.cart);
        return product.remove().then(function(newCart) {
          return $scope.cart = newCart;
        });
      };
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
          return !!USER;
        },
        email: USER
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
      var buy, cart, remove, setCart;
      cart = [];
      setCart = function() {
        if (UserService.isLoggedIn()) {
          return Api.all('cart').getList().then(function(incCart) {
            console.log(incCart);
            return angular.copy(incCart, cart);
          });
        }
      };
      buy = function(product) {
        return cart.all('item').post({
          id: product.id
        }).then(function(cartItem) {
          cart.cartItems.push(cartItem);
          return $rootScope.$broadcast('itemAdded');
        }, function(error) {
          return FlashService.show(error);
        });
      };
      remove = function(item) {
        return console.log(item);
      };
      setCart();
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

  api.factory('DudeService', function() {
    var dudes;
    dudes = [];
    return {
      dudes: dudes,
      add: function(newDude) {
        dudes.push(newDude);
        return dudes;
      },
      remove: function(remDude) {
        angular.forEach(dudes, function(dude, idx) {
          if (remDude.id === dude.id) {
            return dudes.splice(idx, 1);
          }
        });
        return dudes;
      }
    };
  });

}).call(this);

(function() {
  var app;

  app = angular.module('app.utils', []);

  app.factory('Utils', function() {
    return {
      indexById: function(iterable, checkObj) {
        var index;
        index = -1;
        return angular.forEach(iterable, function(obj, i) {
          if (obj.id === checkObj.id) {
            return index = i;
          }
        });
      },
      fromId: function(iterable, checkId) {
        var the_obj;
        the_obj = {};
        return angular.forEach(iterable, function(obj) {
          if (obj.id === checkId) {
            return the_obj = obj;
          }
        });
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
