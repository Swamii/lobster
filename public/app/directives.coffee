app = angular.module 'app.directives', []

app.directive 'pwCheck', ->
    require: 'ngModel'
    link: (scope, elem, attrs, ctrl) ->
      firstPwd = '#' + attrs.pwCheck
      elem.add(firstPwd).on 'keyup', ->
        scope.$apply ->
          v = elem.val() is $(firstPwd).val()
          ctrl.$setValidity 'pwdmatch', v

app.directive 'onEnter', ->
  (scope, element, attrs) ->
    element.bind "keydown keypress", (event) ->
      if event.which is 13
        scope.$apply ->
          scope.$eval(attrs.onEnter)
          event.preventDefault()
