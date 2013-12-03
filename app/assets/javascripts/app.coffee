'use strict'

app = angular.module 'app', ['ui.router', 'app.factories', 'app.controllers']

app.run ['$rootScope', '$state', '$stateParams', ($rootScope, $state, $stateParams) ->
  $rootScope.$state = $state
  $rootScope.$stateParams = $stateParams
]

app.config ['$stateProvider', '$urlRouterProvider', ($stateProvider, $urlRouterProvider) ->

  $urlRouterProvider.otherwise('/dude')

  $stateProvider
  .state 'home',
      url: '/home'
      templateUrl: '/assets/angular/home.html'
      # controller: 'HomeCtrl'
  .state 'dude',
      url: '/dude'
      templateUrl: '/assets/angular/dude-list.html'
      controller: 'DudeCtrl'
      resolve:
        dudes: (Api) ->
          Api.all('dude').getList()
  .state 'dude.detail',
      url: '/{id:[0-9]+}'
      templateUrl: 'assets/angular/dude-detail.html'
      controller: 'DudeDetailCtrl'
      resolve:
        dude: ($stateParams, Api) ->
          Api.one('dude', $stateParams.id).get()
        dudes: (dudes) ->
          dudes
]
