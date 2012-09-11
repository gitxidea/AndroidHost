
var context = new (require('am').Context);
//no dependence action,run after start() immadiate
context.add(function(){
	console.log('start task1');
	setTimeout(function(){
		console.log('set var1')
		context.set('var1',1)
	},100)
	console.log('end task1');
})
//dependence:var1,
//run after var1 is set 
.add('var1',function(){
	console.log('start task2');
	var var1 = context.get('var1');
	setTimeout(function(){
		console.log('set var2')
		context.set('var2',1 + var1)
	},100)
	console.log('end task2');
})
//dependence:var1,var2
//run after var1 and var2 is set
.add('var1','var2',function(){
	console.log('start task3');
	var var1 = context.get('var1');
	var var2 = context.get('var2');
	setTimeout(function(){
		console.log('set var3')
		context.set('var3',var1 * var2)
	},100)
	console.log('end task3');
})
//run after var1 is set(maybe run before the second action)
.add('var1',function(){
	console.log('start task4');
	var var1 = context.get('var1');
	setTimeout(function(){
		console.log('set var4')
		context.set('var4',var1 * var1);
	},100);
	console.log('end task4');
})
//run on stard
.add(function(){
	console.log('start task5');
	console.log('end task5');
}).start();